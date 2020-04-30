##  How to start the project?

1. Download the project to your system.
2. Open the project in any IDE, IntelliJ or Eclipse.
3. The pom.xml file contains all the dependencies which the IDE will sync automatically.
4. You need to setup MongoDB in your system.
5. Create a database 'test' and a collection 'member' in it. Insert a document in member collection who will be the organization admin.
6. Go to src/main/java and under com.lowesforgeeks.api package run LowesForGeeksApplication.java file.
7. APIs can be used now with ObjectId of the created member as loggedInMemberId in the request header.

## Database Schema

1. Event {  
    _id : ObjectId  
    eventType : EventType  
    eventName : String  
    description : String  
    createdBy : Member  
    location : String  
    startDate : String (ISO_LOCAL_DATE_TIME)  
    endDate : String (ISO_LOCAL_DATE_TIME)  
    createdDate : String (ISO_LOCAL_DATE_TIME)  
    numberOfLikes : Integer  
    numberOfViews : Integer  
    numberOfWatchers : Integer  
    numberOfParticipants : Integer  
    recurring : Boolean  
    recurringFrequency : RecurringFrequency  
    expired : Boolean  
    registrations : Array(Registration)  
}  

2. Member {  
    _id : ObjectId  
    firstName : String  
    lastName : String  
    email : String  
    isOrgAdmin : Boolean  
    isTeamAdmin : Boolean  
    teamId : String  
}  

3. Team {  
    _id : ObjectId  
    teamName : String  
    members : Array(Member)  
}  

4. Registration {  
    member : Member  
    responseType : ResponseType  
}  

## Constant Enums

1. EventType {  
    ORGANIZATION,  
    TEAM,  
    PRIVATE  
}  

2. RecurringFrequency {  
    NO,  
    D,  
    W,  
    M,  
    Y  
}  

3. ResponseType {  
    PARTICIPATING,  
    MAYBEPARTICIPATING,  
    NOTPARTICIPATING  
}  

## API Endpoints

BASE URL - http://host:port/lowesforgeeks
All requests have 'loggedInMemberId' passed in request header

1. Event API
    1. /event/create            - to create new event
    2. /event/viewAll           - to view all hosted events based on the roles of logged in member
    3. /event/view/{id}         - to view a particular event
    4. /event/viewTrending      - to view trending events
    5. /event/viewPopular       - to view popular events
    6. /event/viewUpcoming      - to view upcoming events
    7. /event/update/{id}       - to update an event
    8. /event/delete/{id}       - to delete an event

2. Member API
    1. /member/create           - to create a new member
    2. /member/view/{id}        - to view a member
    3. /member/viewAll          - to view all members in organization
    4. /member/update/{id}      - to update a member

3. Team API
    1. /team/create         - create a new team
    2. /team/view/{id}      - to view a team
    3. /team/viewAll        - to view all teams
    4. /team/update/{id}    - to update a team
