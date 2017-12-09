// SERVICES
var AppService = ng.core.Injectable({}).Class({
	constructor : [ ng.http.Http, function(http) {

		var self = this;
		this.authenticated = false;
		this.authenticate = function(credentials, callback) {

			var headers = credentials ? {
				authorization : "Basic " + btoa(credentials.username + ":" + credentials.password)
			} : {};
			http.get('/account/profile', {
				headers : headers
			}).subscribe(function(response) {
				if (response.json().name) {
					self.authenticated = true;
				} else {
					self.authenticated = false;
				}
				callback && callback();
			});

		}

	} ]
})

var RequestOptionsService = ng.core.Class({
	extends : ng.http.BaseRequestOptions,
	constructor : function() {},
	merge : function(opts) {
		opts.headers = new ng.http.Headers(opts.headers ? opts.headers : {});
		opts.headers.set('X-Requested-With', 'XMLHttpRequest');
		return opts.merge(opts);
	}
});

// COMPONENTS
var AppComponent = ng.core.Component({
	templateUrl : 'app.html',
	selector : 'app',
	providers : [ AppService ]
}).Class({
	constructor : [ AppService, ng.http.Http, ng.router.Router, function(app, http, router) {

		this.logout = function() {
			http.post('logout', {}).finally(function() {
				app.authenticated = false;
				router.navigateByUrl('/login')
			}).subscribe();
		}

		app.authenticate();
	} ]
});

var HomeComponent = ng.core.Component({
	templateUrl : 'home.html'
}).Class({
	constructor : [ AppService, ng.http.Http, function(app, http) {
		var self = this;
		this.greeting = {
			id : '',
			msg : ''
		};
		http.get('resource').map(response => response.json()).subscribe(data => self.greeting = data);
		this.authenticated = function() {
			return app.authenticated;
		};
	} ]
});

var LoginComponent = ng.core.Component({
	templateUrl : 'login.html'
}).Class({
	constructor : [ AppService, ng.router.Router, function(app, router) {
		var self = this;
		this.credentials = {
			username : '',
			password : ''
		};
		this.login = function() {
			app.authenticate(self.credentials, function() {
				router.navigateByUrl('/')
			});
			return false;
		};
	} ]
});

//ROUTES
var routes = [
	{
		path : '',
		pathMatch : 'full',
		redirectTo : 'home'
	},
	{
		path : 'home',
		component : HomeComponent
	},
	{
		path : 'login',
		component : LoginComponent
	}
];

// MODULES
var AppModule = ng.core.NgModule({
	imports : [ ng.platformBrowser.BrowserModule, ng.http.HttpModule, ng.router.RouterModule.forRoot(routes), ng.forms.FormsModule ],
	declarations : [ HomeComponent, LoginComponent, AppComponent ],
	providers : [ {
		provide : ng.http.RequestOptions,
		useClass : RequestOptionsService
	} ],
	bootstrap : [ AppComponent ]
}).Class({
	constructor : function() {}
});
