#!/usr/bin/env python

# Copyright 2016 Google Inc.Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


# [ START dev notes ]

# App Engine Images API: https://cloud.google.com/appengine/docs/standard/python/images/usingimages
# Google NDB Entity Property Reference: https://cloud.google.com/appengine/docs/standard/python/ndb/entity-property-reference
# google.appengine.api.users https://cloud.google.com/appengine/docs/standard/python/refdocs/google.appengine.api.users
# Cloud Datastore Query Reference: https://cloud.google.com/datastore/docs/concepts/queries

# NDB Example
# example
# bob = HomeChef(nickname="Bob", email="bob@bob.com", verified= True, phone="5121231234",rating=5)
# bob_key = bob.put() #put Bob's entity in the datastore
# homechef_qry = HomeChef.query()
# homechef_qry = HomeChef.query(HomeChef.email == "bob@bob.com").get() #query database for Bob
#                            (           Filter              )
# [ END dev notes ]


# [START imports]

import os
import urllib
import base64

import smtplib #email
from email.mime.multipart import MIMEMultipart #email
from email.mime.text import MIMEText #email

from google.appengine.api import \
    users  # Google's User API: https://cloud.google.com/appengine/docs/standard/python/refdocs/google.appengine.api.users
from google.appengine.ext import ndb
from google.appengine.ext.webapp import blobstore_handlers
from google.appengine.ext import blobstore

import jinja2
import webapp2

# imports added by hani
import json as newjson
from webapp2_extras import json
import google.appengine.api.images as images

# import for search API
from google.appengine.api import search  # Search API: https://cloud.google.com/appengine/training/fts_intro/lesson2

#imports for authentication
from lib.google.oauth2 import id_token
from lib.google.auth.transport import requests
from google.appengine.api import mail

JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)
# [END imports]

## [START database stuff]
DATASTORE_NAME = 'HomeChef'  # DATASTORE = DATABASE.
MEAL_INDEX_NAME = 'mealindex'
CHEF_INDEX_NAME = 'chefindex'

# this is the id of the Android app as registered in the google cloud console credentials
#CLIENT_ID = '1005512224972-j1o3nb4l4c0tcanlkqce9kohl5ea20o5.apps.googleusercontent.com'
CLIENT_ID = "626635481669-k1al7fp8uumh2fb8oeb59go6uba07i30.apps.googleusercontent.com"



def datastore_key(DATASTORE_NAME):  # think of this as the database,
    # and the classes below are the models (tables)
    return ndb.Key('datastore', DATASTORE_NAME)


##and these are the tables


class Meals(ndb.Model):
    email = ndb.StringProperty(indexed=True)
    name = ndb.StringProperty(indexed=True)
    description = ndb.StringProperty(indexed=False) #not indexed, but needs a property for searching through the whole thing
    quantity = ndb.StringProperty(indexed=False)
    theme = ndb.StringProperty(indexed=True)
    tags = ndb.StringProperty(repeated=True)
    price = ndb.StringProperty(indexed=False)
    meal_image = ndb.BlobKeyProperty()
    meal_image_url = ndb.StringProperty()
    geopt = ndb.GeoPtProperty()


class HomeChef(ndb.Model):
    email = ndb.StringProperty(indexed=True)
    nickname = ndb.StringProperty(indexed=True)  # IDENTIFIER, similar to primary key
    firstname = ndb.StringProperty(indexed=True)
    lastname = ndb.StringProperty(indexed=True)
    profilepic = ndb.StringProperty(indexed=False)
    verified = ndb.BooleanProperty()
    phone = ndb.StringProperty(indexed=False)
    # geopt = ndb.GeoPt(0, 0)  # GeoLocation, this is actually a property on NDB!
    rating = ndb.FloatProperty()  # Float for rating. We can do a 1-5 star system.
    profile_image = ndb.BlobKeyProperty()  # haven't used this before, we should study it
    profile_image_url = ndb.StringProperty()
    subscribed_to = ndb.StringProperty(repeated=True)
    geopt = ndb.GeoPtProperty()

class Tags(ndb.Model):
    name = ndb.StringProperty(indexed=True)



    ##search functionality##
    # list of searchable fields (not sure if GeoField is correct, need to have actual info to test on)
    # lat = geopt.location.lat
    # lon = geopt.location.lat
    #
    # geopoint = search.GeoPoint(lat, lon)
    #
    # fields = [search.TextField(name="Nickname", value=nickname),
    #           search.TextField(name="First Name", value=firstname),
    #           search.TextField(name="Last Name", value=lastname),
    #           search.TextField(name="Verified", value=str(verified)),
    #           search.GeoField(name="Location", value=geopoint)]
    #
    # # creates document containin chef info
    # d = search.Document(fields=fields)
    #
    # # try to add the document to the index
    # try:
    #     add_result = search.Index(name=CHEF_INDEX_NAME).put(d)
    #     print("The document was successfully added to the index.")
    # except search.Error:
    #     print("The document was not successfully added to the index.")


# [START main_page]  - think of the stuff below here as the controllers

#  THIS IS THE METHOD TO SCHEDULE THE CRON JOB FOR.
#  SendSubscriptionEmails send each homechef an email with meals from his/her's subscriptions
class SendSubscriptionEmails(webapp2.RequestHandler):

    def get(self):
        #instantiate email connection, source: https://stackabuse.com/how-to-send-emails-with-gmail-using-python/
        #removed

        #retrieve all homechef objects from database
        homechef_qry = HomeChef.query()
        homechefs = homechef_qry.fetch()

        #For every homechef, do the following:
        for homechef in homechefs:
            #Get the homechef's list of subscriptions
            subscribedTo = homechef.subscribed_to
            #Create a list to store the meals from all that homechef's subscriptions
            mealsFromAllSubscriptions = list()
            #Populate that list with the meals of each subscription
            for email in subscribedTo:
                meals_qry = Meals.query(Meals.email == email)
                meals_result = meals_qry.fetch()
                for meal in meals_result:
                    mealsFromAllSubscriptions.append(meal)

            #Send email to every homechef with a list of meals from his subscriptions
            email_from = 'hnoueilaty@gmail.com'
            email_to = homechef.email
            email_subject = 'Delicious Meals from Your Subscriptions'
            email_body = '<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"></head><body>'

            email_body = ' <a href=" https://abhilasha230587.appspot.com/index.html#/home">Order Now on FamilyFlavors</a></br></br>'
            for meal in mealsFromAllSubscriptions:
                email_body+='<font size="8">'+meal.name+'</font> </br>'
                email_body+='<img src="'+meal.meal_image_url+'style="width:200px;height:150px;"></br>'
                email_body+='<font size="6">'+meal.description+'</font> </br>'
                email_body+='<font size="6">'+meal.price+'</font> </br>'
                email_body+='</br>'
                email_body+='</body></html>'

            #mail.send_mail(sender='hnoueilaty@gmail.com', subject=email_subject, body=email_body)
            mail.send_mail(email_from,email_to,email_subject,email_body)
            #msg = MIMEMultipart()
            #msg['From'] = email_from
            #msg['To'] =  homechef.email
            #msg['Subject'] = email_subject
            #msg.attach(MIMEText(email_body,'plain'))
            #text = msg.as_string()
            #server.sendmail(email_from,email_to,text)

        #server.quit()


        obj = {
                'email_sent':'email sent',
        }
        self.response.content_type = 'application/json'
        self.response.write(json.encode(obj))

class SendSingleEmail (webapp2.RequestHandler):
    def get(self):
        email_to = self.request.get('emailto')
        subscriber = self.request.get('subscribedBy')
        print(email_to)
        print(subscriber)
        email_from = 'hnoueilaty@gmail.com'
        email_subject = 'Congratulations! You were just subscribed by {}!'.format(subscriber)
        email_body = 'Congratulations! You were just subscribed by {}!'.format(subscriber)
        mail.send_mail(email_from, email_to, email_subject, email_body)

        obj = {
            'email_sent': 'email sent'
        }
        self.response.content_type = 'application/json'
        self.response.write(json.encode(obj))




class subscribeToHomeChef(webapp2.RequestHandler):
    def get(self):
        print("Inside subscribeToChef get")

        # Get the emails
        homechef1_email = self.request.get('homechef1_email')
        homechef2_email = self.request.get('homechef2_email')

        # Get homechef1
        homechef1_qry = HomeChef.query(HomeChef.email == homechef1_email)
        homechef1 = homechef1_qry.fetch()

        subs = homechef1[0].subscribed_to
         # following code ensure there are no duplicate entries in subscribed_to array.
        # NDB does not inherently check for duplicates in repeated property.
        if(homechef2_email in subs):
            pass
        else:
            homechef1[0].subscribed_to.append(homechef2_email)
            homechef1[0].put()


        # If the code made it to here, should be good.
        self.response.content_type = 'application/json'
        obj = {
            'subscribed': 'true',
            'subscribed_by' : homechef1_email,
            'subscribed_to' : homechef2_email
        }
        self.response.write(json.encode(obj))


class loadMainData(webapp2.RequestHandler):
    def get(self):
        print("Inside loadMainData get")
        user = users.get_current_user()  # method to get te current user
        if user:  # if the user is logged in
            url = users.create_logout_url(self.request.uri)  # httplink
            url_linktext = 'Logout'  # button
            obj = {
                'url': url,
                'url_linktext': url_linktext,
                'user_email': user.email()
            }
        else:
            url = users.create_login_url(self.request.uri)  # httplink
            url_linktext = 'Login'  # button
            obj = {
                'url': url,
                'url_linktext': url_linktext
            }

        self.response.write(json.encode(obj))


class IsAuthenticated(webapp2.RequestHandler):  # UNTESTED
    # CHECK TO SEE IF THE USER IS AUTHENTICATED VIA GOOGLE. IF USER IS NOT.... SEND TO LOGIN PAGE OR RETURN FALSE?
    # self.redirect("/CreateProfile")
    def get(self):
        # Get the HomeChef profile belonging to the email supplied
        print('inside IsAuthenticated GET')
        email = self.request.get('email')

        # Authentication Check
        print('printing email')
        print(email)
        AuthenticationStatus = 'false'
        user = users.get_current_user()
        if user:
            if (user.email() == email):
                AuthenticationStatus = 'true'
        else:
            AuthenticationStatus = 'false'

        # Send JSON (OR REDIRECT HERE)
        self.response.content_type = 'application/json'
        obj = {
            'AuthenticationStatus': AuthenticationStatus,  # Returns TRUE/FALSE BOOLEAN
        }
        self.response.write(json.encode(obj))


class CheckProfileExist(webapp2.RequestHandler):
    # INPUT: EMAIL       OUTPUT: profileExist boolean
    def get(self):
        email = self.request.get('email')
        print("GET CheckProfileExist : "+ email)
        #homechef_qry = HomeChef.query()
        homechef_qry = HomeChef.query(HomeChef.email == email)
        homechef = homechef_qry.fetch()   #you have to do a specific fetch

        print(homechef)
        if (homechef ):
            profileExist = 'true'

        else:
            profileExist = 'false'

        print(profileExist)

        self.response.content_type = 'application/json'
        obj = {
            'profileExist': profileExist
        }
        self.response.write(json.encode(obj))


class GetProfile(webapp2.RequestHandler):
    # THIS ONE WILL ACTUALLY SEND A HOMECHEF OBJECT
    def get(self):
        email = self.request.get('email')
        hoemchef_qry = HomeChef.query()
        homechef_qry = HomeChef.query(HomeChef.email == email)
        #profirstname = homechef_qry.firstname
        #prolastname = homechef_qry.lastname
        #prophotoURL = homechef_qry.profile_image_url
        #proemail = homechef_query.email

        # return HomeChef object
        self.response.content_type = 'application/json'
        obj = {
            'HomeChef': homechef_qry,
            #'email': proemail,
            #'firstname': profirstname,
            #'lastname': prolastname,
            #'photoURL': prophotoURL,
        }
        self.response.write(json.encode(obj))


class ListMealsForChef(webapp2.RequestHandler):


    def get(self):
        email = self.request.get('email')
        meals_qry = Meals.query(Meals.email == email)

        meals_result = meals_qry.fetch()  # Iterate through attributes

        mealslist = list()
        for meal in meals_result:

            obj = {
                'mealname': meal.name,
                'mealdes': meal.description,
                'mealq': meal.quantity,
                'mealp':meal.price,
                'meal_image':meal.meal_image_url,
                'meal_theme':meal.theme,
                'meal_tag':meal.tags,
                'meal_latitude': meal.geopt.lat,
                'meal_longitude': meal.geopt.lon
            }
            mealslist.append(obj)

        self.response.content_type = 'application/json'
        self.response.write(json.encode(mealslist))

class CreateMeal(webapp2.RequestHandler):
    def get(self):
        print("inside CREATE MEAL")
        upload_url = blobstore.create_upload_url('/rest/uploadMealImage')
        print(upload_url)

        template_values = {
            'upload_url': upload_url,
        }

        self.response.write(json.encode(template_values))


    def post(self):
        print("Inside CreateMeal post")



class MealPhotoUploadHandler(blobstore_handlers.BlobstoreUploadHandler):
    def post(self):

        x = self.get_uploads()
        upload = self.get_uploads()[0]

        # Get input values from the form
        email = self.request.get('email')
        title = self.request.get('title')
        description = self.request.get('description')
        price = self.request.get('price')
        quantity = self.request.get('quantity')
        theme = self.request.get('theme')
        tags = self.request.get('tags')
        tagsarr = tags.split(',')

        #GEOTAG 
        latitude = self.request.get('latitude')
        longitude = self.request.get('longitude')
        if latitude == '' and longitude == '':
            loc = "{},{}".format(30.285104, -97.737554)
        else:
            loc = "{},{}".format(latitude,longitude)


        meals = Meals(geopt=ndb.GeoPt(loc),email=email, name=title, description=description, quantity=quantity,price=price,theme=theme,tags=tagsarr,
                          meal_image=upload.key(), meal_image_url=images.get_serving_url(upload.key()))

        meals.put()

        for tag in tagsarr:
            # ADD
            key_a = ndb.Key(Tags, tag);
            t = Tags(key=key_a,name=tag)
            t.put()

            # Insert unique
            a = Tags.get_or_insert(tag)


        obj = {'newmealcreated': 'true'}
        self.redirect('/')

        ##search functionality##
        # list of searchable fields
        fields = [search.TextField(name="Title", value=title),
                  search.TextField(name="Description", value=description)]

        # creates document containin chef info
        d = search.Document(fields=fields)

        # try to add the document to the index
        try:
            add_result = search.Index(name=MEAL_INDEX_NAME).put(d)
            print("The document was successfully added to the index.")
        except search.Error:
            print("The document was not successfully added to the index.")
        obj = {'newprofilecreated': 'true'}
        self.redirect('/')

class SearchMeals(webapp2.RequestHandler):
    def get(self):
        # searches title and description
        print("inside GET of SearchMeals")
        tags = self.request.get('tags')
        if(tags):
            tags = tags.split(',')
        print("tags : ",tags,len(tags))
        theme = self.request.get('theme')
        if(theme):
            theme = theme.split(',')
        print("theme : ",theme,len(theme))

        meals_qry = Meals.query()

        if(len(theme)>0):
            condition1 = Meals.theme.IN(theme)
            print("condition1 : ",condition1)
            meals_qry = Meals.query(condition1)

        if(len(tags)>0):
            condition2 = Meals.tags.IN(tags)
            print("condition2" , condition2)
            if(len(theme)>0):
                meals_qry = Meals.query(condition1,condition2)
            else:
                meals_qry = Meals.query(condition2)



        #meals_qry = Meals.query(condition1,condition2)
        print(meals_qry)
        meals_result = meals_qry.fetch()  # Iterate through attributes

        mealslist = list()
        for meal in meals_result:
            obj = {
                'mealname': meal.name,
                'mealdes': meal.description,
                'mealq': meal.quantity,
                'mealp': meal.price,
                'meal_image': meal.meal_image_url,
                'meal_theme': meal.theme,
                'meal_tag': meal.tags,
                'meal_chef':meal.email,
                'meal_latitude': meal.geopt.lat,
                'meal_longitude': meal.geopt.lon
            }
            mealslist.append(obj)

        self.response.content_type = 'application/json'
        self.response.write(json.encode(mealslist))

class GetTags(webapp2.RequestHandler):
    def get(self):
        print("inside GetTags")
        tags_qry = Tags.query()
        tags_result = tags_qry.fetch()

        print('::::::::::::::::::::',tags_result)

        tagslist = list()
        for tag in tags_result:
            obj = {'tag':tag.name}
            tagslist.append(obj)

        self.response.content_type = 'application/json'
        self.response.write(json.encode(tagslist))


class ChefSearch(webapp2.RequestHandler):
    def get(self):
        firstname = self.request.get('firstname')
        lastname = self.request.get('lastname')
        firstname = firstname.lower()
        lastname = lastname.lower()

        #if the user searched by both firstname and lastname
        if(firstname != '' and lastname != ''):
            homechef_qry = HomeChef.query(HomeChef.firstname.lower().IN([firstname]))
            homechef_qry =HomeChef.query(HomeChef.lastname.lower().IN([lastname]))
            
        #if the user searched only by firstname
        elif(firstname != '' and lastname == ''):
            homechef_qry = HomeChef.query(HomeChef.firstname.lower().IN([firstname]))

        #if the user searched only by lastname 
        elif(firstname == '' and lastname != ''):
            homechef_qry = HomeChef.query(HomeChef.lastname.lower().IN([lastname]))


        #Run the query 
        homechefs_results = homechef_qry.fetch(50)

        #Create an empty list of objects
        homechefs_result_list = List()

        #For each homechef result, add the object to list
        for HomeChef in homechef_results:
            obj = {
                'email': HomeChef.email,
                'nickname': HomeChef.nickname,
                'firstname': HomeChef.firstname,
                'lastname': HomeChef.lastname,
                'profilepic': HomeChef.profilepic,
                'verified': HomeChef.verified,
                'phone': HomeChef.phone,
                'rating':HomeChef.rating,
                'profile_image': HomeChef.profile_image,
                'profile_image_url': HomeChef.profile_image_url,
            }
            homechefs_result_list.append(obj)

        self.response.content_type = 'application/json'
        self.response.write(json.encode(homechefs_result_list))

    # searchString used for first/last name and nickname (all) for now
    # add geopoint search (not sure how yet)

class ChefSearchByGeotag(webapp2.RequestHandler): #SOURCE: https://stackoverflow.com/questions/40550590/geospatial-location-based-search-in-google-appengine-python
    def post(self):
        pass


class ProfilePhotoUploadHandler(blobstore_handlers.BlobstoreUploadHandler):
    def post(self):

        x = self.get_uploads()
        upload = self.get_uploads()[0]

        # Get input values from the form
        print("INSIDE CREATE Profile post request")
        firstname = self.request.get('firstname')
        lastname = self.request.get('lastname')
        # phone = self.request.get('tel')
        email = self.request.get('email')
        subs = []

        # GEOTAG
        latitude = self.request.get('latitude')
        longitude = self.request.get('longitude')
        if latitude == '' and longitude == '':
            loc = "{},{}".format(30.285104, -97.737554)
        else:
            loc = "{},{}".format(latitude, longitude)


        homechef_qry = HomeChef.query(HomeChef.email == email).get()
        if (homechef_qry is None):
            homechef = HomeChef(geopt=ndb.GeoPt(loc),subscribed_to=subs, email=email, nickname=email, firstname=firstname,lastname=lastname, profile_image=upload.key(),profile_image_url=images.get_serving_url(upload.key()))
            homechef.put()
        #added to handle Android post request for updating profile
        else:
            homechef_qry.profile_image = upload.key()
            homechef_qry.profile_image_url = images.get_serving_url(upload.key())
            homechef_qry.put()

        obj = {'newprofilecreated': 'true'}
        self.redirect('/')

class CreateProfile(webapp2.RequestHandler):

    def get(self):
        print("inside CREATE PROFILE")
        upload_url = blobstore.create_upload_url('/rest/uploadProfileImage')
        print(upload_url)

        template_values = {
            'upload_url': upload_url,
        }

        self.response.write(json.encode(template_values))


class HomeChefs(webapp2.RequestHandler):

    def get(self):
        homechef_qry = HomeChef.query()
        homechefs = homechef_qry.fetch(50)

        chefslist= list()
        for chef in homechefs:
            obj = {
                'chef_nickname': chef.nickname,
                'chef_firstname': chef.firstname,
                'chef_lastname': chef.lastname,
                'chef_email': chef.email,
                'chef_image': chef.profile_image_url

            }
            chefslist.append(obj)

        self.response.content_type = 'application/json'
        self.response.write(json.encode(chefslist))




class APIOutputTest(webapp2.RequestHandler):
    print("Inside APIOutputTest")

    # https://stackoverflow.com/questions/12664696/how-to-properly-output-json-with-app-engine-python-webapp2
    def get(self):
        print("Inside APIOutputTest - get request")
        self.response.content_type = 'application/json'
        obj = {
            'success': 'some var',
            'payload': 'some var',
        }
        self.response.write(json.encode(obj))


class GetProfile(webapp2.RequestHandler):
    #  MAKE SURE THIS RETURNS JSON WITH HOMECHEF DATA AND IMAGE URL!!!!

    def get(self):
        email = self.request.get('email')
        #hoemchef_qry = HomeChef.query()
        homechef_qry = HomeChef.query(HomeChef.email == email)


        homechef_result = homechef_qry.fetch()  # Iterate through attributes
        firstname = homechef_result[0].firstname;
        lastname = homechef_result[0].lastname;
        image = homechef_result[0].profile_image_url
        subscribed_to = homechef_result[0].subscribed_to
        geoloc = homechef_result[0].geopt

        # return HomeChef object
        self.response.content_type = 'application/json'
        obj = {
            'firstname': firstname,
            'lastname': lastname,
            'imageURL': image,
            'subscribed_to':subscribed_to,
            'latitude' : geoloc.lat,
            'longitude' : geoloc.lon

        }
        self.response.write(json.encode(obj))


####Authentication Code testing
class AuthPage(webapp2.RequestHandler):
     def post(self):
         username = self.request.get('username')
         token = self.request.get('token')

         result = ""
         # based on the example here: https://developers.google.com/identity/sign-in/android/backend-auth
         try:
             # Specify the CLIENT_ID of the app that accesses the backend:
             idinfo = id_token.verify_oauth2_token(token, requests.Request(), CLIENT_ID)

             if idinfo['iss'] not in ['accounts.google.com', 'https://accounts.google.com']:
                 result = " bad issuer"
                 raise ValueError('Wrong issuer.')

             # ID token is valid. Get the user's Google Account ID from the decoded token.
             userid = idinfo['sub']
             result = " valid!"
         except ValueError, e:
             result = " " + str(e)
             # Invalid token
             pass

         self.response.write('signed in user is ' + username + result)




def jsonDefault(object):
    return object.__dict__

# [START app]
app = webapp2.WSGIApplication([
    ('/rest/loadMainData', loadMainData),
    # ('/rest/sign', Guestbook),
    ('/rest/CreateProfile', CreateProfile),
    ('/rest/uploadProfileImage', ProfilePhotoUploadHandler),
    ('/rest/getProfile',GetProfile),
    ('/rest/HomeChefs', HomeChefs),
    ('/rest/CreateMeal', CreateMeal),
    ('/rest/getMealsforUser',ListMealsForChef),
    ('/rest/uploadMealImage',MealPhotoUploadHandler),
    ('/rest/IsAuthenticated', IsAuthenticated),
    ('/rest/checkProfileExists', CheckProfileExist),
    ('/rest/searchMeals',SearchMeals),
    ('/rest/getTagsList',GetTags),
    ('/rest/subscribeToHomeChef', subscribeToHomeChef),
    ('/rest/APIOutputTest', APIOutputTest),
    ('/rest/SendSubscriptionEmails', SendSubscriptionEmails),
    ('/rest/SendSingleEmail',SendSingleEmail),
    #AuthenticationTesting endpoint
    ('/',AuthPage),

    #('/rest/ImgServ', ImgServe),

], debug=True)
# [END app]


