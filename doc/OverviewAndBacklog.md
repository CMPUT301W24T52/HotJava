### Stages
```mermaid
%%{init: { 'logLevel': 'debug', 'themeVariables': {'fontSize': '30px', 'fontFamily': 'Inter'}, 'theme': 'dark', 'timeline': {'disableMulticolor': true}}}%%
timeline
	title Hot Java Staging
	section Design, Prototyping and Implementation
	  Splash and Home Screen 
		: Splash Screen
		: Home Page
		: Notifications
		: My Events
		: Other Primitive Functionality
	  Implementing User Types
		: Admin 
		: Organizer
		: Attendee
	  Designing Database
		: Implement Users Collections
		: Implement Events Collections
	  Event Actionable Content 
		: Event Details interactions
		: Event Check-in's
		: Adding and Removing Events
	section Implementation, Integration and Testing
	  Implement QR Properties
		  : Integration with DB
		  : Client-side Scanning feature (attendee)
		  : Client-side display QR Feature (organizer)
	  Integration of Group Components
		  : Ensuring app component "Plug-and-play"
		  : Integration into "Main" activity
		  
	  Refine Existing Design
		  : Contemplate area in need of refinement
		  : Evaluate program flow
		  : Consult with team
		  
	  
	  
	section Completion and Further Refinement
	  Items to Revisit
		  : Colorway
		  : Layout (sharp vs rounded)
		  : User Friendly?
```
### Gantt Timeline
Subject to change (add as necessary)
#### Topics
	Database
	Homepage/Splash
	User Permissions
	Events and Functionality
		QR functionality
	Notifications
		Milestones
	Primitive Functionality
	Additional Features
		Geolocation
	Frontend Layout (final)
		Colors, Shapes, Edges

```mermaid
gantt
    dateFormat  YYYY-MM-DD
    title       Timeline
	Start : milestone, 2024-02-14, 1d
	Halfway : milestone, crit, 2024-03-08, 1d
	End : milestone, 2024-04-08, 1d
	Documentation (Throughout)            :active,    des1, 2024-02-14, 2024-04-08

    section Design (Idea -> Prototype)
    Project Planning          :done, PP, 2024-02-08, 2024-02-16
    Home Page Functionality   :active,  HPF, 2024-02-15, 1w
    Database Design           :active,  DBD, 2024-02-15, 4d
	Event Page Design         :         EPD, after HPF, 2d
	Profile Page Design       :         PPD, after HPF, 2d

	Notifications Design      :         ND, after PPD, 2d
	Milestones Design         :         MD, after ND, 1d
	Geolocation Features Design :       GFD, after DBPF, 1d

    section Implementation <br> (Prototype -> code)
    Db Skeleton               :crit, DBS, after DBD, 2d
    User Types and Permissons :crit, UTP, after DBD, 3d
	Splash/Home Page          :crit, SPH, after HPF, 5d
	Pofile Page               :      PP, after PPD, 3d
	Event Page                :      EP, after EPD, 3d
	QR Functionality          :crit,  QRF, after EP, 4d
	Notifications             :      N, after ND, 3d
	Milestones                :      M, after N, 2d
	
	Geolocation Features      :       GF, 2024-03-10, 2d
	
    section Integration <br> (Group-component synthesis)
    Componentize Main Activity                  :crit, after SPH, 2d
    Database connections to App                 :crit, DBCtAS, after SPH, 2d
    Integrate Profile and Event Pages           :crit, IPEP, after DBCtAS, 4d
    Uniform DB Functionality Across app         :crit, UFA, after DBCtAS, 3d
    Db and primitive Funtionality              :crit, DBPF, after DBCtAS, 5d
    Notification Integration                   :after DBT, 2d
    Db and Primitive Functionality Complete    :milestone, after DBPF, 1d

	Geolocation Integration   : GI, after GF, 2d
	
    section Testing <br> (Minor Changes and Revisions)
    HomePage Testing         :          HT, after SPH, 1d
    Db Testing               :          DBT, after DBCtAS, 1d

	Geolocation Testing      : GT, after GI, 1d
    
    Primitive Func Test 1    :milestone, after DBPF, 1d
    
    
    
    
```
###### Legend:
<ul>
<p style = "color : red"> Critical</p>
<p> Done </p>
<p style = "color : #a03fc0"> Inactive</p>
<p style = "color : grey"> Active</p>
</ul>

### High Level Program Flow
(To be updated as needed)
```mermaid
%%{init:{"flowchart": {"useMaxWidth": "true" }}}%%
flowchart LR
	HomePage --> User
	User --> |attendee| AHomePage
	User -->|organizer| OHomePage
	HomePage -->|admin| adminPage

	AHomePage --> EventsPage --> MyEvents
	MyEvents --> CheckIn
	MyEvents --> SaveEvent
	AHomePage --> Profile

	OHomePage --> CreateEvent
	OHomePage --> ViewEvents
	
	ViewEvents --> EditEvent
	ViewEvents --> DeleteEvent
	ViewEvents --> EventAttendeeList

	adminPage --> ManageEvents
	adminPage --> ManageUsers
	
```
