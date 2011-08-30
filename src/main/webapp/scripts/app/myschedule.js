 require([ 
          	"dojo/dom",
          	"dojo/on",
          	"dijit/registry",
           	"dojo/parser",
           	"dojox/widget/Standby",
           	"dijit/form/FilteringSelect",
           	"dijit/form/TextBox",
           	"dijit/form/RadioButton",
           	"dijit/form/RadioButton",
           	"dijit/form/CheckBox",
           	"dijit/Dialog",
           	"dijit/form/SimpleTextarea",
           	"dijit/form/CurrencyTextBox",
           	"dijit/form/NumberSpinner",
           	"dijit/form/DateTextBox",
            "dijit/Menu",
            "dijit/MenuItem",
            "dijit/form/DropDownButton",
   			"dojo/ready" 
   		], function(dom,on,registry,parser,Menu, MenuItem,DropDownButton) {
		 	parser.parse();
		 	
		 	
		 	
// *******javascript at top of page
			
			var editservicecounter = 0;
			var jCalTargetValue = "";
			var currentselecteddate = "";
			var global_selected_staff = "";
			function appointmentobject(staffid,appointmentstatus,appointmentid,appointmentdate,serviceid,servicename,price,clientid,firstname,lastname,notes,fnctbeginhr,fnctbeginmin,fnctbeginampm,fnctendhr,fnctendmin,fnctendampm,service1type,recur_parent,request_indicator)
			 {
				this.staffid = staffid;
				this.appointmentstatus= appointmentstatus;
				this.appointmentdate = appointmentdate;
				this.appointmentid = appointmentid;
				this.serviceid = serviceid;
				this.servicename = servicename;
				this.service1type = service1type;
				this.price = price;
				this.clientid = clientid;
				this.firstname=firstname;
				this.lastname=lastname;
				this.notes = notes;
				this.beginhr = fnctbeginhr;
				this.beginmin = fnctbeginmin;
				this.beginampm = fnctbeginampm;
				this.endhr = fnctendhr;
				this.endmin = fnctendmin;
				this.endampm = fnctendampm;
				this.recur_parent = recur_parent;
				this.request_indicator = request_indicator;
			 }	
			function clientobject(clientid,fullname){
				this.clientid = clientid;
				this.fullname = fullname;
			}
			function serviceobject(serviceid,description,amountoftime){
				this.serviceid = serviceid;
				this.description = description;
				this.amountoftime = amountoftime;
			}
			function categoryserviceobject(categoryid,serviceid,description,amountoftime){
				this.categoryid = categoryid;
				this.serviceid = serviceid;
				this.description = description;
				this.amountoftime = amountoftime;
			}
			function servicepriceobject(serviceid,cost){
				this.serviceid = serviceid;
				this.cost = cost;
			}
			function customservicepriceobject(appointmentid, serviceid,cost){
				this.appointmentid = appointmentid;
				this.serviceid = serviceid;
				this.cost = cost;
			}
			
			var appointmentsArray = [];
			var clientsArray = [];
			var servicesArray = [];
			var categoryservicesArray = [];
			var servicespricesArray = [];
			var customServicesPricesArray = [];
			
			<c:forEach var="client" items="${clients}">
				var placeholder = "${client}";
				if(placeholder != "="){
					var clientcoln = placeholder.indexOf('=');
					var clientid = placeholder.substring(0,clientcoln);
					var clientname = placeholder.substring(clientcoln+1,placeholder.length);
					var newclient = new clientobject(clientid,clientname);
					clientsArray.push(newclient);
				}
			</c:forEach>
			
			<c:forEach var="serviceprice" items="${servicesprices}">
				var placeholder = "${serviceprice}";
				if(placeholder != "="){
					var servicecoln = placeholder.indexOf('=');
					var serviceid = placeholder.substring(0,servicecoln);
					var serviceprice = placeholder.substring(servicecoln+1,placeholder.length);
					var serviceprice = new servicepriceobject(serviceid,serviceprice);
					servicespricesArray.push(serviceprice);
				}
			</c:forEach>
			
			<c:forEach var="service" items="${services}">
				var placeholder = "${service}";
				if(placeholder != "="){
					var servicecoln = placeholder.indexOf('=');
					var serviceid = placeholder.substring(0,servicecoln);
					var servicedescription = placeholder.substring(servicecoln+1,placeholder.length);
					var servicecoln2 = servicedescription.indexOf('(');
					var servicecoln3 = servicedescription.indexOf(')');
					var servicetime = servicedescription.substring(servicecoln2+1,servicecoln3);
					servicedescription = servicedescription.substring(0,servicecoln2);
					var service = new serviceobject(serviceid,servicedescription,servicetime);
					servicesArray.push(service);
				}
			</c:forEach>
			<c:forEach var="catservice" items="${categoryservices}">
				var serviceid = "${catservice.svcid}";
				var servicedescription = "${catservice.servicedescription}";
				var servicetime = "${catservice.svcamounttime}";
				var catid = "${catservice.catid}";
				
				var service = new categoryserviceobject(catid, serviceid,servicedescription,servicetime);
				categoryservicesArray.push(service);
			</c:forEach>

			<c:forEach var="appointment" items="${todaysagenda}">
				<fmt:formatDate var="formattedappt" value='${appointment.appointmentDate}' pattern="yyyy-MM-dd"/>
				<fmt:formatNumber var="formattedServiceCost" value='${appointment.service1cost}' currencySymbol='$' type='currency'/>
				var sbeginetimex = "${appointment.s_beginDateTime}";
				var coln = sbeginetimex.indexOf(':');
				var uniquebeginhour = sbeginetimex.substring(0,coln);
				
				var uniquebeginmin = sbeginetimex.substring(coln+1,coln+3);
				if(uniquebeginmin == 0){
					uniquebeginmin = '00';
				}
				
				var ap = sbeginetimex.substring(sbeginetimex.length,sbeginetimex.length-2);
				var uniquebeginampm = "";
				uniquebeginampm = ap.toLowerCase();

				// end time
				var sendetimex = "${appointment.s_endDateTime}";
				var endcoln = sendetimex.indexOf(':');
				var uniqueendhour = sendetimex.substring(0,endcoln);
				
				var uniqueendmin = sendetimex.substring(endcoln+1,endcoln+3);
				if(uniqueendmin == 0){
					uniqueendmin = '00';
				}
				
				var uniqueendap = sendetimex.substring(sendetimex.length,sendetimex.length-2);
				var uniqueendampm = "";
				uniqueendampm = uniqueendap.toLowerCase();
				var getappt = new appointmentobject("${appointment.staff.id}","${appointment.status}","${appointment.id}","${formattedappt}","${appointment.service1id}","${appointment.servicename1}","${appointment.service1cost}","${appointment.client.id}","${appointment.client.firstName}","${appointment.client.lastName}","${appointment.notes}",uniquebeginhour,uniquebeginmin,uniquebeginampm,uniqueendhour,uniqueendmin,uniqueendampm,"${appointment.recur_parent}","${appointment.requested_image_path}");
				appointmentsArray.push(getappt);	

			</c:forEach>
			
			function toTitleCase(str)
			{
				return str.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
			}	
			var StillNeedsValidating = true;
			var svcstandbydlg;
			var standby;
			var av1standby;
			var standbydlg;
			var week_already_loaded = 'false';
			var month_already_loaded = 'false';
			var usethisstore;
			var svcid = "";
			var svcname = "";
			var inputEvents = [];  // Global var

			function loadCreate(){
				var createwdgt = registry.byId("createTab");
				if(createwdgt.get("display") == "none"){
					createwdgt.set("display","show");
				} else{
					createwdgt.set("display","block");
				}
				var rawdate = registry.byId("c_editselectdate").attr("value");
				var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
				displayEditTodaysAgenda(postdate);
			}	
			
			function loadMonth(){
				var date = new Date();
				var d = date.getDate();
				var m = date.getMonth();
				var y = date.getFullYear();
				var exists = false;
				try{
					if ( $('#monthcalendar').children().length > 0 ){
						exists = true;
						$('#monthcalendar').fullCalendar('refetchEvents');
						$('#monthcalendar').fullCalendar( 'rerenderEvents' )
					}
				}catch(e){
					console.log(e);
				}
				if(exists == false) {

					$('#monthcalendar').fullCalendar({
						defaultView: 'month',
						editable: true,
						events: function(start, end, callback) {
								start = $.fullCalendar.formatDate( start, "yyyy-MM-dd" );
								end = $.fullCalendar.formatDate( end, "yyyy-MM-dd" );
								standby.show();
								$.ajaxSetup({ cache: false });
								$.getJSON("myschedule/calendar?date="+start+"&amp;enddate="+end,
										function(data){
											
											// console.log("the
											// data: "+data);
											var events =  [];
											var tdata = JSON.stringify(data);
											var parsedobjectdata;
											var isJSON;
											try {
												// console.log(tdata);
												parsedobjectdata = JSON.parse(tdata);
												// console.log(parsedobjectdata);
												isJSON = true;
											}
											catch (e) {
												isJSON = false;
											}
											
											// console.log(parsedobjectdata);
											if(parsedobjectdata.constructor == Array){
												var eventcolorbackground = 'yellow';
												var cntr = 0;
												for(var key in parsedobjectdata) {
													if(global_selected_staff != ''){
														if(parsedobjectdata[key].staff.id != global_selected_staff){
															continue;
														}
													}
													if(parsedobjectdata[key].status == 'CANCELED'){
														continue;
													}
													if(parsedobjectdata[key].status == 'DELETED'){
														continue;
													}
													if(parsedobjectdata[key].status == 'ACTIVE'){
														eventcolorbackground = 'active';
													}
													if(parsedobjectdata[key].status == 'NO_SHOW'){
														eventcolorbackground = 'no_show';
													}
													if(parsedobjectdata[key].status == 'CHECKED_OUT'){
														eventcolorbackground = 'checked_out';
													}
													
													var month = parsedobjectdata[key].month_ApptDate;
													var day = parsedobjectdata[key].day_ApptDate;
													var year = parsedobjectdata[key].year_ApptDate;
													var bhour = data[key].fc_beginHour;
													var bminute = data[key].fc_beginMinute;
													var ehour = data[key].fc_endHour;
													var eminute = data[key].fc_endMinute;
													
													var apptdate = new Date(year,month-1,day,bhour,bminute);
													var enddate = new Date(year,month-1,day,ehour,eminute);

													var titledesc = "";
													if(parsedobjectdata[key].client != null){
														titledesc = parsedobjectdata[key].client.firstName + " " + parsedobjectdata[key].client.lastName + " ";
														titledesc = titledesc + parsedobjectdata[key].servicename1 + " " + parsedobjectdata[key].service1cost;
													}else{
														titledesc = " TIME BLOCK "+ parsedobjectdata[key].personallabel;
														eventcolorbackground = "timeblock";
													}
													
													// console.log(apptdate);
													events.push({
														title: titledesc,
														start: apptdate,
														end: enddate,
														className: eventcolorbackground,
														allDay: false
													});
												}
												
												callback(events);
												
											}						
											// console.log("after
											// jsonify "+tdata);
									standby.hide();		
								});					
								// when data is ready use
								// callback(events);
								
						},
						 eventClick: function(calEvent, jsEvent, view) {
								if (event.url) {
									// window.open(event.url);
									alert('event click');
									return false;
								}
							}

					});			
		}
			}
			function loadWeek(){
				var date = new Date();
				var d = date.getDate();
				var m = date.getMonth();
				var y = date.getFullYear();
				var variablemintime = 0;
				var variablemaxtime = 24;
				var exists = false;
				try{
					if ( $('#weekcalendar').children().length > 0 ){
						exists = true;
						$('#weekcalendar').fullCalendar('refetchEvents');
						$('#weekcalendar').fullCalendar( 'rerenderEvents' )
					}
				}catch(e){
					console.log(e);
				}
				if(exists == false) {
						
						$('#weekcalendar').fullCalendar({
							header: {
								left: 'prev,next today',
								center: 'title',
								right: ''
							},
							defaultView: 'agendaWeek',
							editable: true,
							slotMinutes: 15,
							aspectRatio : 0.9,
							firstHour : 10 ,
							minTime : variablemintime,
							maxTime : variablemaxtime,
							selectable: true,
							selectHelper: true,
							 eventClick: function(event) {
									if (event.url) {
										// window.open(event.url);
										alert('event click');
										return false;
									}
								},		
							eventRender: function(event, element, view)
										  {
										  element.bind('click', function()
												 {
												 var day = ($.fullCalendar.formatDate( event.start, 'dd' ));
												 var month = ($.fullCalendar.formatDate( event.start, 'MM' ));
												 var year = ($.fullCalendar.formatDate( event.start, 'yyyy' ));
												  // alert(year+'-'+month+'-'+day);
												 });
										   },
							// Clicked on day
							dayClick: function(date, allDay, jsEvent, view) {
									// alert('entered day click');
									if (allDay) {
											// alert('Clicked on the
											// entire day: ' +
											// date);
									} else{
											// alert('Clicked on the
											// slot: ' + date);
									}

							},
							// Selected a Time
							select: function( startDate, endDate, allDay, jsEvent, view ) {
								// alert(startDate);
							},			
							events: function(start, end, callback) {
									start = $.fullCalendar.formatDate( start, "yyyy-MM-dd" );
									end = $.fullCalendar.formatDate( end, "yyyy-MM-dd" );
									standby.show();
									$.ajaxSetup({ cache: false });
									$.getJSON("myschedule/calendar?date="+start+"&amp;enddate="+end,
											function(data){
												console.log("the data: "+data);
												var events =   [];
												var tdata = JSON.stringify(data);
												var parsedobjectdata;
												var isJSON;
												try {
													console.log(tdata);
													parsedobjectdata = JSON.parse(tdata);
													console.log(parsedobjectdata);
													isJSON = true;
												}
												catch (e) {
													isJSON = false;
												}
												
												console.log(parsedobjectdata);
												if(parsedobjectdata.constructor == Array){
													var eventcolorbackground = 'yellow';
													var cntr = 0;
													for(var key in parsedobjectdata) {
														if(global_selected_staff != ''){
															if(parsedobjectdata[key].staff.id != global_selected_staff){
																continue;
															}
														}
														if(parsedobjectdata[key].status == 'CANCELED'){
															continue;
														}
														if(parsedobjectdata[key].status == 'DELETED'){
															continue;
														}
														if(parsedobjectdata[key].status == 'ACTIVE'){
															eventcolorbackground = 'active';
														}
														if(parsedobjectdata[key].status == 'NO_SHOW'){
															eventcolorbackground = 'no_show';
														}
														if(parsedobjectdata[key].status == 'CHECKED_OUT'){
															eventcolorbackground = 'checked_out';
														}
														var month = parsedobjectdata[key].month_ApptDate;
														var day = parsedobjectdata[key].day_ApptDate;
														var year = parsedobjectdata[key].year_ApptDate;
														var bhour = data[key].fc_beginHour;
														var bminute = data[key].fc_beginMinute;
														var ehour = data[key].fc_endHour;
														var eminute = data[key].fc_endMinute;
														
														var apptdate = new Date(year,month-1,day,bhour,bminute);
														var enddate = new Date(year,month-1,day,ehour,eminute);
														var titledesc = "";
														if(parsedobjectdata[key].client != null){
															titledesc = parsedobjectdata[key].client.firstName + " " + parsedobjectdata[key].client.lastName + " ";
															titledesc = titledesc + parsedobjectdata[key].servicename1 + " " + parsedobjectdata[key].service1cost;
														}else{
															titledesc = " TIME BLOCK "+ parsedobjectdata[key].personallabel;
															eventcolorbackground = "timeblock";
														}
														
														console.log(apptdate);
														events.push({
															title: titledesc,
															start: apptdate,
															end: enddate,
															editable: true,
															className: eventcolorbackground,
															allDay: false
														});
													}
													
													callback(events);
													
												}						
												console.log("after jsonify "+tdata);
										standby.hide();
									});					
									// when data is ready use
									// callback(events);
									
									
							},
						});			
				}	
			}
			function destroy(col){
				console.log('agenda view display: '+$('#agendaview'+col).is(':visible'));
				if($('#agendaview'+col).is(':visible')){
					toggleVis(col,false);	
					// $('#divcollabel'+col).hide();
					// $('#agendaview'+col).fullCalendar( 'destroy'
					// );
					// $('#agendaview'+col).hide();
				}else{
					toggleVis(col,true);
					// $('#divcollabel'+col).show();
					// $('#agendaview'+col).fullCalendar( 'render'
					// );
					// $('#agendaview'+col).fullCalendar('refetchEvents');
					$('#agendaview'+col).fullCalendar( 'rerenderEvents' )
					
					// $('#agendaview'+col).show();
				}
			}
			function isNumber(n) {
				  return !isNaN(parseFloat(n)) &amp;&amp; isFinite(n);
			}	
			function loadAgenda1(optional_day){
				console.log("optional_day: "+optional_day);
				var date = null;
				if(optional_day==undefined){
					console.log("about to do a new date on jCalTarget");
					
					console.log("jCalTarget"+jCalTargetValue);
					date = new Date(jCalTargetValue);
					
				}else{
					date = optional_day;
				}
				
				console.log("loadAgenda1 date: "+date);
				var d = date.getDate();
				if(date != 'Invalid Date'){
					// d = d + 1;
				}
				var m = date.getMonth();
				var y = date.getFullYear();
				var variablemintime = 0;
				var variablemaxtime = 24;
				var exists = false;
				if(av1standby == null){
					av1standby = new dojox.widget.Standby({
						  target: "agendaview1"
						});
					document.body.appendChild(av1standby.domNode);
				}
				try{
					if ( $('#agendaview1').children().length > 0 ){
						exists = true;
						if(isNumber(y) &amp;&amp; isNumber(m) &amp;&amp; isNumber(d)){
							$('#agendaview1').fullCalendar( 'gotoDate', y , m, d );	
						}
						
						$('#agendaview1').fullCalendar('refetchEvents');
						$('#agendaview1').fullCalendar( 'rerenderEvents' );
						
					}
				}catch(e){
					console.log(e);
				}
				if(exists == false) {
						
						$('#agendaview1').fullCalendar({
							header: {
								left: '',
								center: 'title',
								right: ''
							},
							resources: [
							        	<c:forEach var="staff" items="${staffs}" varStatus="status">
										{
											id: ${staff.key},
											name: '${staff.value}'
										}
										<c:if test="${not status.last}">
										,
										</c:if>
						        	</c:forEach>

									],
							defaultView: 'resourceDay',
							editable: true,
							allDaySlot: false,
							axisFormat: 'h(:mm)tt',
							timeFormat: 'h(:mm)tt',
							slotMinutes: 15,
							aspectRatio : 0.9,
							firstHour : 8 ,
							minTime : variablemintime,
							maxTime : variablemaxtime,
							selectable: true,
							selectHelper: true,
							eventRender: function(event, element, view)
										  {
											if(event.requested_image_path != null){
												if(event.requested_image_path == 'yellow'){	
													element.find('.fc-event-time').addClass("afc-event-requested-yellow");
												}
												if(event.requested_image_path == 'red'){	
													element.find('.fc-event-time').addClass("afc-event-requested-red");
												}
											}
								  			element.bind('mouseover', function()
											 {
								  				
											    var tooltip = '<div class="tooltipevetn" style="width:200px;height:100px;background:yellow;color:blue;position:absolute;z-index:10001;">name: ' + event.clientfirstname+' '+event.clientlastname + '<br/>service: '+event.servicename+'<br/>home phone: '+event.clientphonenumber+'<br/>work phone: '+event.clientworkphonenumber+'<br/>cell phone: '+event.clientcellphonenumber+'</div>';
											    $("body").append(tooltip);
											    $(this).mouseover(function(e) {
											        $(this).css('z-index', 10000);
											        $('.tooltipevetn').fadeIn('500');
											        $('.tooltipevetn').fadeTo('10', 1.9);
											    }).mousemove(function(e) {
											        $('.tooltipevetn').css('top', e.pageY + 10);
											        $('.tooltipevetn').css('left', e.pageX + 20);
											    });

											 });
								  			element.bind('mouseout', function()
													 {
										  				
													    $(this).css('z-index', 8);
													    $('.tooltipevetn').remove();


													 });
								
										  element.bind('click', function()
												 {
													var day = ($.fullCalendar.formatDate( event.start, 'dd' ));
													var month = ($.fullCalendar.formatDate( event.start, 'MM' ));
													var year = ($.fullCalendar.formatDate( event.start, 'yyyy' ));
													if(event.clientid == ''){
														displayPersonalDialogForFCCalendar(event,true);
													}else{
														registry.byId("isThisEditAgendaViewFlag").set("value","true");
														setEditApptDialogWithFCEvent(event);
													}
												 });
										   },
							// Clicked on day
							// dayClick: function(date, allDay,
							// jsEvent, view) {
								
									// alert('entered day click');
									// if (allDay) {
											// alert('Clicked on the
											// entire day: ' +
											// date);
									// } else{
											// alert('Clicked on the
											// slot: ' + date);
											
									// }

							// //},
							// Selected a Time
							select: function( startDate, endDate, allDay, jsEvent, view,resourceObj ) {
								// alert("Selected times:
								// "+startDate + " to " +endDate + "
								// staff: "+view.id);
								registry.byId('avChooseTBorCADialog').reset();
								registry.byId("choosewhattodo_startdatetime").set("value",startDate);
								registry.byId("choosewhattodo_enddatetime").set("value",endDate);
								registry.byId("choosewhattodo_staffid").set("value",view.id);
								registry.byId('isThisADDAgendaViewFlag').set("value","true");
								setEditTab();
								registry.byId("avChooseTBorCADialog").show();
								
							},			
							events: function(start, end, callback) {
									start = $.fullCalendar.formatDate( start, "yyyy-MM-dd" );
									end = $.fullCalendar.formatDate( end, "yyyy-MM-dd" );
									av1standby.show();
									$.ajaxSetup({ cache: false });
									

										$.getJSON("myschedule/calendar?date="+start+"&amp;enddate="+end,
												function(data){
													console.log("the data: "+data);
													var events =   [];
													var tdata = JSON.stringify(data);
													var parsedobjectdata;
													var isJSON;
													try {
														console.log(tdata);
														parsedobjectdata = JSON.parse(tdata);
														console.log(parsedobjectdata);
														isJSON = true;
													}
													catch (e) {
														isJSON = false;
													}
													
													console.log(parsedobjectdata);
													if(parsedobjectdata.constructor == Array){
														customServicesPricesArray = [];
														var eventcolorbackground = 'yellow';
														var cntr = 0;
														for(var key in parsedobjectdata) {
															if(global_selected_staff != ''){
																if(parsedobjectdata[key].staff.id != global_selected_staff){
																	continue;
																}
															}
															if(parsedobjectdata[key].status == 'CANCELED'){
																continue;
															}
															if(parsedobjectdata[key].status == 'DELETED'){
																continue;
															}
															if(parsedobjectdata[key].status == 'ACTIVE'){
																eventcolorbackground = 'active';
															}
															if(parsedobjectdata[key].status == 'NO_SHOW'){
																eventcolorbackground = 'no_show';
															}
															if(parsedobjectdata[key].status == 'CHECKED_OUT'){
																eventcolorbackground = 'checked_out';
															}
															
															var month = parsedobjectdata[key].month_ApptDate;
															var day = parsedobjectdata[key].day_ApptDate;
															var year = parsedobjectdata[key].year_ApptDate;
															var bhour = data[key].fc_beginHour;
															var bminute = data[key].fc_beginMinute;
															var ehour = data[key].fc_endHour;
															var eminute = data[key].fc_endMinute;
															
															var apptdate = new Date(year,month-1,day,bhour,bminute);
															var enddate = new Date(year,month-1,day,ehour,eminute);
															var titledesc = "";
															var staffid = "";
															var clientid = "";
															var clientfirstname = "";
															var clientlastname = "";
															// BEGIN
															// OF
															// DOAJAX
															// CODE
															var sbeginetimex = parsedobjectdata[key].s_beginDateTime;
															var sendetimex = parsedobjectdata[key].s_endDateTime;
															var iscancelled = false;
															if(parsedobjectdata[key].cancelled == null || parsedobjectdata[key].cancelled == 'false'){
																iscancelled = false;
															}else{
																iscancelled = parsedobjectdata[key].cancelled;
															}
															var yearapptdate = apptdate.getFullYear();
															var monthapptdate = apptdate.getMonth()+1;
															var dayapptdate = apptdate.getDate();
															// the
															// realdate
															// is
															// used
															// to
															// set
															// the
															// calendar
															// widget
															var realdate = new Date(apptdate.getUTCFullYear(),apptdate.getUTCMonth(),apptdate.getUTCDate());
															var endtime = new Date(parsedobjectdata[key].endDateTime.time);
															var endhour = endtime.getHours();
															var endmin = endtime.getMinutes();
															var eap = "am";
															if (endhour   > 11) { eap = "pm";        }
															if (endhour   > 12) { endhour = endhour - 12; }
															if (endhour   == 0) { endhour = 12;        }
															
															var begintime = new Date(parsedobjectdata[key].beginDateTime.time);
															var beginhour = begintime.getHours();
															var beginmin = begintime.getMinutes();
															var ap = "am";
															if (beginhour   > 11) { ap = "pm";        }
															if (beginhour   > 12) { beginhour = beginhour - 12; }
															if (beginhour   == 0) { beginhour = 12;        }
				
															var coln = sbeginetimex.indexOf(':');
															// beginhour
															// used
															// to
															// set
															// the
															// value
															// in
															// the
															// begin
															// hour
															// select
															// box
															beginhour = sbeginetimex.substring(0,coln);
															beginmin = sbeginetimex.substring(coln+1,coln+3);
															if(beginmin == 0){
																beginmin = '00';
															}
															// the
															// begin
															// am pm
															// value
															ap = sbeginetimex.substring(sbeginetimex.length,sbeginetimex.length-2);
				
															// end
															// time
															var coln = sendetimex.indexOf(':');
															endhour = sendetimex.substring(0,coln);
															endmin = sendetimex.substring(coln+1,coln+3);
															if(endmin == 0){
																endmin = '00';
															}
															eap = sendetimex.substring(sendetimex.length,sendetimex.length-2);
															id = parsedobjectdata[key].id;
															var svccost = "";
															if(parsedobjectdata[key].service1cost == null){
																svccost = "0.00";
															}else{
																svccost = parsedobjectdata[key].service1cost;
																svc1id = parsedobjectdata[key].service1id;
																console.log("loading events for fc calendar svccost: "+svccost);
																var custserviceprice = new customservicepriceobject(id,svc1id,svccost);
																customServicesPricesArray.push(custserviceprice);
																
															}
															
															
															if(parsedobjectdata[key].servicename1 == null){
															}else{
																svcname = parsedobjectdata[key].servicename1;
															}
															
															var svctype = "";
															if(parsedobjectdata[key].service1type == null){
															}else{
																svctype = parsedobjectdata[key].service1type;
															}
															
															
															if(parsedobjectdata[key].service1id == null){
															}else{
																svcid = parsedobjectdata[key].service1id;
															}
															
															var stateStore = null;
															id = id.substring(id.length-2,id.length);
															
															var id = "0";
															id = parsedobjectdata[key].id;
															
															var personallabel = '';
															if(parsedobjectdata[key].personallabel != null){
																personallabel = parsedobjectdata[key].personallabel;
															}
															
															var notes = "";
															if(parsedobjectdata[key].notes == null){
															}else{
																notes = decodeURI(decodeURIComponent(parsedobjectdata[key].notes));
																if(notes != null){
																	notes =notes.split('+').join(' ');
																}
															}													
															// END
															// OF
															// DOAJAX
															var clientphonenumber = "";
															clientphonenumber = parsedobjectdata[key].clientphonenumber;
															if(clientphonenumber == 'undefined'){
																clientphonenumber = '';
															}
															var clientcellphonenumber = "";
															clientcellphonenumber = parsedobjectdata[key].clientcellphonenumber;
															if(clientcellphonenumber == 'undefined'){
																clientcellphonenumber = '';
															}
															var clientworkphonenumber = "";
															clientworkphonenumber = parsedobjectdata[key].clientworkphonenumber;
															if(clientworkphonenumber == 'undefined'){
																clientworkphonenumber = '';
															}
															
															if(parsedobjectdata[key].client != null){
																clientid = parsedobjectdata[key].client.id;
																
																clientfirstname = parsedobjectdata[key].client.firstName;
																clientlastname = parsedobjectdata[key].client.lastName;
																titledesc = parsedobjectdata[key].client.firstName + " " + parsedobjectdata[key].client.lastName + " ";
																titledesc = titledesc + parsedobjectdata[key].servicename1 + " " + parsedobjectdata[key].service1cost;
															}else{
																titledesc = " TIME BLOCK "+ parsedobjectdata[key].personallabel;
																eventcolorbackground = "timeblock";
															}
															if(parsedobjectdata[key].staff != null){
																staffid = parsedobjectdata[key].staff.id;
															}
															var recur_parent = parsedobjectdata[key].recur_parent;
															var requested_image_path = parsedobjectdata[key].requested_image_path;
															console.log("staffid: "+staffid);
															console.log(apptdate);
															events.push({
																requested_image_path: requested_image_path,
																clientphonenumber: clientphonenumber,
																clientcellphonenumber: clientcellphonenumber,
																clientworkphonenumber: clientworkphonenumber,
																resourceId: parsedobjectdata[key].staff.id,
																className: eventcolorbackground,
																textColor: 'white',
																title: titledesc,
																realdate:realdate,
																clientid:clientid,
																clientfirstname:clientfirstname,
																clientlastname:clientlastname,
																appointmentid:id,
																appointmentnotes: notes,
																appointmentstatus:''+parsedobjectdata[key].status,
																appointmentdate:apptdate,
																servicename:svcname,
																servicetype:svctype,
																serviceid:svcid,
																staffid:''+staffid,
																stafffirstname:'',
																stafflastname:'',
																personallabel: personallabel,
																personalreason: personalreason,
																beginhour:beginhour,
																beginmin:beginmin,
																beginampm:ap,
																endhour:endhour,
																endmin:endmin,
																endampm:eap,
																price:svccost,
																start: apptdate,
																end: enddate,
																editable: true,
																allDay: false,
																recur_parent:recur_parent 
															});
														}
														
														callback(events);
														
													}						
													console.log("after jsonify "+tdata);
													av1standby.hide();
										});			
									
									// when data is ready use
									// callback(events);
									var edittab = registry.byId("editTab");
									// edittab.set("style","height:
									// 750px");

									
							},
						});			
				}
			}	
// ***** end of topofpage
 
			 /*
				 * creates a json object from the data input
				 */
			function Trax(response){
				var isJSON;
				var data;
				try {
					// console.log(response);
					data = JSON.parse(response);
					console.log(data);
					isJSON = true;
				}
				catch (e) {
					isJSON = false;
				}

				if (isJSON) {
					// data is already parsed, so just use it
					// handle response codes
					// do something with returned data
					
					if(data.constructor == Array){
						
						var cntr = 0;
						var myCars=new Array(); // regular array
												// (add an optional
												// integer
						for(var key in data) {
							bigdata = new Object();
							bigdata.id = data[key].id;
							bigdata.description = data[key].description + " ("+data[key].amounttime+")";
						
							myCars[cntr]=bigdata;       						
							cntr = cntr + 1;
						}
						person = new Object();
						person.items = myCars;
						person.identifier = 'id';
						person.label = 'description';
						return person;
						
					}else{
						console.log(data.id);
						console.log(data.value);
						bigdata = new Object();
						bigdata.id = data.id;
						bigdata.description = data.description;
						
						
						var myCars=new Array(); // regular array
												// (add an optional
												// integer
						myCars[0]=bigdata;       						
						
						person = new Object();
						person.items = myCars;
						person.identifier = 'id';
						person.label = 'description';
						return person;
						
						
					}
				}
			 }
			function fillServices(id){
				
				console.log("svcid "+id);
				
				  standbydlg = new dojox.widget.Standby({
					  target: "editApptDialog"
				  });
				document.body.appendChild(standbydlg.domNode);
				standbydlg.show();
				var deferred = dojo.xhrGet( {
					url : "myschedule/jsonapptsvc?apptid="+id,
					handleAs: "json",
					preventCache: true,
					load : function(data, newValue) {
							console.log("the data: "+data);
							var svcid = data[0].id;
							var svcname = data[0].amounttime;
							stateStore = data;
							var tdata = Trax(JSON.stringify(stateStore));
							
							var pstore = new dojo.data.ItemFileReadStore({
										   'data':tdata
									  });
							console.log(tdata);
							
							try {
								var widg1=registry.byId('editApptDialog'); 
								var cwidget = registry.byId("editselect");
								if(cwidget){
									cwidget.reset();
									registry.byId('progButton').set('label','Select one');registry.byId('progButton').set('value','');
									cwidget.store = pstore;
									cwidget.setValue(svcid);
								}else{
									var box = new registry.form.FilteringSelect({
										id: "editselect",
										name: "service",
										value: svcid,
										displayedValue:svcname,
										autocomplete  : true,
										required      : true,
										 onChange      : function(action) {setEditEndTime(); },
										store: pstore,
										searchAttr: "description"
									}, "editselect");			
									box.setValue(svcid);
								}
								
								console.log(widg1);
							 }catch(e){
									var box = new registry.form.FilteringSelect({
										id: "editselect",
										name: "service",
										autocomplete  : true,
										value: svcid,
										displayedValue:svcname,
										required      : true,
										 onChange      : function(action) {setEditEndTime(); },
										store: pstore,
										searchAttr: "description"
									}, "editselect");			      
									box.setValue(svcid);   
							 }
							 standbydlg.hide();
							 return data;
						},
					error: function(error) {
								console.log("An unexpected error occurred: " + error);
								standbydlg.hide();
								window.location.reload();
							}													
				});			
				return deferred;
			}
			function addTimeBlockDialog(){
				var cwtd_startdatetime = registry.byId("choosewhattodo_startdatetime").attr("value");
				var cwtd_enddatetime = registry.byId("choosewhattodo_enddatetime").attr("value");
				var cwtd_staffid = registry.byId("choosewhattodo_staffid").attr("value");
				
				var id = cwtd_staffid;
				var newValue = new Date(cwtd_startdatetime);
				var timeblock_enddate = new Date(cwtd_enddatetime);
				var timeblockwidget = registry.byId("c_personalselectdate");
				registry.byId("isThisAgendaViewFlag").set("value","true");
				var dayte = "";
				try{
					dayte = dojo.date.locale.format(newValue, {datePattern: "yyyy-MM-dd", selector: "date"});
				}catch(err){
					dayte = newValue;
				}
				document.getElementById("hPersonalSelectDate").value=dayte;
				timeblockwidget.set("value",newValue);
					
				$('#updatepersonalbutton').hide();
				$('#savepersonalbutton').show();
				
				var iscancelled = false;

				var apptdate = newValue;
				var yearapptdate = apptdate.getFullYear();
				var monthapptdate = apptdate.getMonth()+1;
				var dayapptdate = apptdate.getDate();

				var endhour = timeblock_enddate.getHours();
				var endmin = timeblock_enddate.getMinutes();
				var eap = "am";
				if (endhour   > 11) { eap = "pm";        }
				if (endhour   > 12) { endhour = endhour - 12; }
				if (endhour   == 0) { endhour = 12;        }
				
				var beginhour = newValue.getHours();
				var beginmin = newValue.getMinutes();
				var ap = "am";
				if (beginhour   > 11) { ap = "pm";        }
				if (beginhour   > 12) { beginhour = endhour - 12; }
				if (beginhour   == 0) { beginhour = 12;        }

				var staffforappt = cwtd_staffid;
				var staffwidget = registry.byId("fapptstaff_personal");
				if(staffwidget){
					staffwidget.set("value",staffforappt);
				}
		
				var fwidget = registry.byId("personalhour");
				if(fwidget){
					fwidget.setValue(beginhour);
				}
				
				var editmin = registry.byId("personalminute");
				if(beginmin == 0 || beginmin == '0'){
					beginmin = '00';
				}
				if(beginmin){
					editmin.setValue(new String(beginmin));
				}
				var editampm = registry.byId("personalampm");
				if(editampm){
					editampm.setValue(ap.toLowerCase());
				}

				// end time
				var fewidget = registry.byId("personalendhour");
				if(fewidget){
					fewidget.setValue(endhour);
				}
				
				var endeditmin = registry.byId("personalendminute");
				if(endmin == 0 || endmin == '0'){
					endmin = '00';
				}
				if(endmin){
					endeditmin.setValue(new String(endmin));
				}
				var endeditampm = registry.byId("personalendampm");
				if(endeditampm){
					endeditampm.setValue(eap.toLowerCase());
				}				
				
				registry.byId('personaledit').set("value","false");
				registry.byId('personalDialog').show();
				
			}						
			
			function createAppointmentForAgendaView(){
				registry.byId("fedithour").setDisabled(false);
				registry.byId("editminute").setDisabled(false);
				registry.byId("editampm").setDisabled(false);
				registry.byId("editdialogprice").setDisabled(false);
				registry.byId("editdialognotes").setDisabled(false);
				registry.byId("editsavebutton").setDisabled(false);
				registry.byId("editcancelbutton").setDisabled(false);
				registry.byId("editnsbutton").setDisabled(false);
				registry.byId("editcobutton").setDisabled(false);
				
				var cwtd_startdatetime = registry.byId("choosewhattodo_startdatetime").attr("value");
				var cwtd_enddatetime = registry.byId("choosewhattodo_enddatetime").attr("value");
				var cwtd_staffid = registry.byId("choosewhattodo_staffid").attr("value");
				registry.byId("c_editclientname_id").setAttribute('disabled', false);
				registry.byId('editApptDialog').reset();
				registry.byId('progButton').set('label','Select one');registry.byId('progButton').set('value','');
				registry.byId("isThisEditAgendaViewFlag").set("value","true");
				registry.byId('isThisADDAgendaViewFlag').set("value","true");
				setEditTab();
				var	svcname = "";
				var svctype = "";
				var svcid = "";
				
				var dsrvselect = '';
				var iscancelled = false;

				var apptdate = new Date(cwtd_startdatetime);
				var apptenddate = new Date(cwtd_enddatetime);
				var yearapptdate = apptdate.getFullYear();
				var monthapptdate = apptdate.getMonth()+1;
				var dayapptdate = apptdate.getDate();
	
				var calendar = registry.byId("c_editselectdate");
				console.log('calendar value: '+calendar.value);
				calendar.set('value',apptdate);
				
				var staffforappt = cwtd_staffid;
				
				var endhour = apptenddate.getHours();
				var endmin = zeroFill(apptenddate.getMinutes(),2);
				var eap = "am";
				if (endhour   > 11) { eap = "pm";        }
				if (endhour   > 12) { endhour = endhour - 12; }
				if (endhour   == 0) { endhour = 12;        }
				
				var beginhour = apptdate.getHours();
				var beginmin = apptdate.getMinutes();
				var ap = "am";
				if (beginhour   > 11) { ap = "pm";        }
				if (beginhour   > 12) { beginhour = beginhour - 12; }
				if (beginhour   == 0) { beginhour = 12;        }
	
				var staffwidget = registry.byId("fapptstaff_edit");
				if(staffwidget){
					console.log("STAFF FOR APPT IS: " +staffforappt);
					staffwidget.set("value",staffforappt);
				}
				
				var fwidget = registry.byId("fedithour");
				if(fwidget){
					
					fwidget.set("value",beginhour);
				}
				
				var editmin = registry.byId("editminute");
				if(beginmin == 0 || beginmin == '0'){
					beginmin = '00';
				}
				if(editmin){
					editmin.set("value",new String(beginmin));
				}
				var editampm = registry.byId("editampm");
				if(editampm){
					editampm.set("value",ap.toLowerCase());
				}
				// end time
				var fewidget = registry.byId("fendedithour");
				if(fewidget){
					console.log("endhour: "+endhour);
					fewidget.set("value",endhour);
				}
				var endeditmin = registry.byId("endeditminute");
				if(endmin == 0){
					endmin = '00';
				}
				if(endeditmin){
					console.log("endmin: "+endmin);
					endeditmin.set("value",endmin);
				}
				var endeditampm = registry.byId("endeditampm");
				if(endeditampm){
					console.log("eap.toLowerCase(): "+eap.toLowerCase());
					endeditampm.set("value",eap.toLowerCase());
				}
				
			 	var edittabs = registry.byId("myRecurTabContainer");
			 	edittabs.startup();
			 	edittabs.resize();
			 	
			 	
				var dlg = registry.byId('editApptDialog');
				dlg.show();
				// registry.byId("hideShowRecur").domNode.style.display
				// = 'block'
				setEditTab();
			}
			function setEditTab(){
				var addAppt2 = registry.byId('isThisADDAgendaViewFlag').get("value");
				if(addAppt2 == "true"){
					dojo.style("hideShowRecur", 'display', '');
					dojo.style("editDialogTab2Message", 'display', 'none');
				}else{
					dojo.style("hideShowRecur", 'display', 'none');
					dojo.style("editDialogTab2Message", 'display', '');
				}
			}
			function setEditApptDialogWithFCEvent(event){
				registry.byId('editApptDialog').reset();
				registry.byId('progButton').set('label','Select one');registry.byId('progButton').set('value','');
				registry.byId("isThisEditAgendaViewFlag").set("value","true");
				registry.byId('isThisADDAgendaViewFlag').set("value","false");
				setEditTab();
				var status = event.appointmentstatus;
				var firstname =  "";
				var lastname = "";
				if(event.clientid != null){
					firstname = event.clientfirstname;
					lastname = event.clientlastname;
					registry.byId("c_editclientname_id").set('value',event.clientid);
					registry.byId("c_editclientname_id").setAttribute('disabled', true);
				}
				
				if( status == 'CHECKED_OUT'){
					registry.byId("fedithour").setDisabled(true);
					registry.byId("editminute").setDisabled(true);
					registry.byId("editampm").setDisabled(true);
					registry.byId("editdialogprice").setDisabled(true);
					registry.byId("editdialognotes").setDisabled(true);
					registry.byId("editsavebutton").setDisabled(true);
					registry.byId("editcancelbutton").setDisabled(true);
					registry.byId("editnsbutton").setDisabled(true);
					registry.byId("editcobutton").setDisabled(true);
				}else{
					registry.byId("fedithour").setDisabled(false);
					registry.byId("editminute").setDisabled(false);
					registry.byId("editampm").setDisabled(false);
					registry.byId("editdialogprice").setDisabled(false);
					registry.byId("editdialognotes").setDisabled(false);
					registry.byId("editsavebutton").setDisabled(false);
					registry.byId("editcancelbutton").setDisabled(false);
					registry.byId("editnsbutton").setDisabled(false);
					registry.byId("editcobutton").setDisabled(false);
					
				}
				if(event.requested_image_path != null){
					if(event.requested_image_path == 'yellow'){	
            			registry.byId("progButton").set("label","Yellow star");
            			registry.byId("progButton").set("value","yellow");
					}
					if(event.requested_image_path == 'red'){	
            			registry.byId("progButton").set("label","Red star");
            			registry.byId("progButton").set("value","red");
					}
				}
				if(event.servicename == null){
				}else{
					svcname = event.servicename;
				}
				
				var svctype = "";
				if(event.servicetype == null){
				}else{
					svctype = event.servicetype;
				}
				
				
				if(event.serviceid == null){
				}else{
					svcid = event.serviceid;
				}
				
				var dsrvselect = '';
				dsrvselect = 'editselect';
				var srvselect = registry.byId(dsrvselect);
				if(srvselect){
					srvselect.set("value",svcid);
				}						
				var iscancelled = false;
				if(status != 'CANCELED'){
					iscancelled = false;
				}else{
					iscancelled = true;
				}
				var apptdate = event.appointmentdate;// new
														// Date(event.appointmentdate);
				var yearapptdate = apptdate.getFullYear();
				var monthapptdate = apptdate.getMonth()+1;
				var dayapptdate = apptdate.getDate();
	
				var calendar = registry.byId("c_editselectdate");
				console.log('calendar value: '+calendar.value);
				var realdate = event.realdate;
				calendar.set('value',realdate);
				
				var staffforappt = event.staffid;
				
				var endhour = event.endhour;
				var endmin = zeroFill(event.endmin,2);
				var eap = event.endampm;
				
				var beginhour = event.beginhour;
				var beginmin = event.beginmin;
				var ap = event.beginampm;
	
				var staffwidget = registry.byId("fapptstaff_edit");
				if(staffwidget){
					console.log("STAFF FOR APPT IS: " +staffforappt);
					staffwidget.set("value",staffforappt);
				}
				
				var fwidget = registry.byId("fedithour");
				if(fwidget){
					
					fwidget.set("value",beginhour);
				}
				
				var editmin = registry.byId("editminute");
				if(beginmin == 0 || beginmin == '0'){
					beginmin = '00';
				}
				if(editmin){
					editmin.set("value",new String(beginmin));
				}
				var editampm = registry.byId("editampm");
				if(editampm){
					editampm.set("value",event.beginampm.toLowerCase());
				}
	
				// end time
				var fewidget = registry.byId("fendedithour");
				if(fewidget){
					
					fewidget.set("value",endhour);
				}
				
				var endeditmin = registry.byId("endeditminute");
				if(endmin == 0){
					endmin = '00';
				}
				if(endeditmin){
					endeditmin.set("value",endmin);
				}
				var endeditampm = registry.byId("endeditampm");
				if(endeditampm){
					endeditampm.set("value",event.endampm.toLowerCase());
				}
				
				var svccost = "";
				if(event.price == null){
					svccost = "0.00";
				}else{
					svccost = event.price;
				}
	
					
				var stateStore = null;
				
				var id = "0";
				id = event.appointmentid;
				
				var notes = "";
				if(event.appointmentnotes == null){
				}else{
					notes = event.appointmentnotes;
				}
				
				var dapptid = '';
				dapptid = 'editdialogapptid';
	
				var d = registry.byId(dapptid);
				d.set("value",id);
				
				var erp = "0";
				erp = event.recur_parent;
				var e_r_p = registry.byId('edit_recur_parent');
				e_r_p.set("value",erp);
				
				
				var dsrv = '';
				dsrv = 'editdialogservice';
	
				var srv = registry.byId(dsrv);
				srv.set("value",svcname);
	
				var dsrvt = '';
				dsrvt = 'editdialogservicetype';
	
				var srvtype = registry.byId(dsrvt);
				srvtype.set("value",svctype);
	
				var dsrvti = '';
				dsrvti = 'editdialogsvcid';
	
				var servid = registry.byId(dsrvti);
				servid.set("value",svcid);
	
				var dpr = '';
				dpr = 'editdialogprice';
	
				var pr = dom.byId(dpr);
				console.log("setEditApptDialogWithFCEvent svccost: "+svccost);
				pr.value =formatCurrency(svccost);
				
				var dno = '';
				dno = 'editdialognotes';
	
				var dnotes = dom.byId(dno);
				dnotes.value =notes;
				
				var dlg = registry.byId('editApptDialog');
				dlg.show();
				
			}
			function noajax(aid){
				StillNeedsValidating=false;
				standby.hide();
				
				if(appointmentsArray.length > 0){
					registry.byId('editApptDialog').reset();
					registry.byId('progButton').set('label','Select one');registry.byId('progButton').set('value','');
					registry.byId("isThisEditAgendaViewFlag").set("value","false");
					registry.byId('isThisADDAgendaViewFlag').set("value","false");
					setEditTab();
					for(var key in appointmentsArray) {
						var findappt = appointmentsArray[key];
						if(findappt.appointmentid == aid){
							var status = findappt.appointmentstatus;
							var firstname =  "";
							var lastname = "";
							if(findappt.clientid != null){
								firstname = findappt.firstname;
								lastname = findappt.lastname;
								registry.byId("c_editclientname_id").set('value',findappt.clientid);
								registry.byId("c_editclientname_id").setAttribute('disabled', true);
							}
							
							if( status == 'CHECKED_OUT'){
								registry.byId("fedithour").setDisabled(true);
								registry.byId("editminute").setDisabled(true);
								registry.byId("editampm").setDisabled(true);
								registry.byId("editdialogprice").setDisabled(true);
								registry.byId("editdialognotes").setDisabled(true);
								registry.byId("editsavebutton").setDisabled(true);
								registry.byId("editcancelbutton").setDisabled(true);
								registry.byId("editnsbutton").setDisabled(true);
								registry.byId("editcobutton").setDisabled(true);
							}else{
								registry.byId("fedithour").setDisabled(false);
								registry.byId("editminute").setDisabled(false);
								registry.byId("editampm").setDisabled(false);
								registry.byId("editdialogprice").setDisabled(false);
								registry.byId("editdialognotes").setDisabled(false);
								registry.byId("editsavebutton").setDisabled(false);
								registry.byId("editcancelbutton").setDisabled(false);
								registry.byId("editnsbutton").setDisabled(false);
								registry.byId("editcobutton").setDisabled(false);
								
							}
							if(findappt.servicename == null){
							}else{
								svcname = findappt.servicename;
							}
							
							var svctype = "";
							if(findappt.servicetype == null){
							}else{
								svctype = findappt.servicetype;
							}
							
							
							if(findappt.serviceid == null){
							}else{
								svcid = findappt.serviceid;
							}
							
							var dsrvselect = '';
							dsrvselect = 'editselect';
							var srvselect = registry.byId(dsrvselect);
							if(srvselect){
								srvselect.set("value",svcid);
							}						
							var iscancelled = false;
							if(status != 'CANCELED'){
								iscancelled = false;
							}else{
								iscancelled = true;
							}
							var apptdate = new Date(findappt.appointmentdate);
							var yearapptdate = apptdate.getFullYear();
							var monthapptdate = apptdate.getMonth()+1;
							var dayapptdate = apptdate.getDate();
			
							var calendar = registry.byId("c_editselectdate");
							console.log('calendar value: '+calendar.value);
							var realdate = new Date(apptdate.getUTCFullYear(),apptdate.getUTCMonth(),apptdate.getUTCDate());
							calendar.set('value',realdate);
							
							var staffforappt = findappt.staffid;
							
							var endhour = findappt.endhr;
							var endmin = zeroFill(findappt.endmin,2);
							var eap = findappt.endampm;
							
							var beginhour = findappt.beginhr;
							var beginmin = findappt.beginmin;
							var ap = findappt.beginampm;
			
							var staffwidget = registry.byId("fapptstaff_edit");
							if(staffwidget){
								console.log("STAFF FOR APPT IS: " +staffforappt);
								staffwidget.set("value",staffforappt);
							}
							
							var fwidget = registry.byId("fedithour");
							if(fwidget){
								
								fwidget.set("value",beginhour);
							}
							
							var editmin = registry.byId("editminute");
							if(beginmin == 0 || beginmin == '0'){
								beginmin = '00';
							}
							if(editmin){
								editmin.set("value",new String(beginmin));
							}
							var editampm = registry.byId("editampm");
							if(editampm){
								editampm.set("value",ap.toLowerCase());
							}
			
							// end time
							var fewidget = registry.byId("fendedithour");
							if(fewidget){
								
								fewidget.set("value",endhour);
							}
							
							var endeditmin = registry.byId("endeditminute");
							if(endmin == 0){
								endmin = '00';
							}
							if(endeditmin){
								endeditmin.set("value",endmin);
							}
							var endeditampm = registry.byId("endeditampm");
							if(endeditampm){
								endeditampm.set("value",eap.toLowerCase());
							}
							
							var svccost = "";
							if(findappt.price == null){
								svccost = "0.00";
							}else{
								svccost = findappt.price;
							}

								
							var stateStore = null;
							
							var id = "0";
							id = findappt.appointmentid;
							
							var notes = "";
							if(findappt.notes == null){
							}else{
								notes = findappt.notes;
							}
							
							var dapptid = '';
							dapptid = 'editdialogapptid';
			
							var d = registry.byId(dapptid);
							d.set("value",id);
							
							var erp = "0";
							erp = findappt.recur_parent;
							var e_r_p = registry.byId('edit_recur_parent');
							e_r_p.set("value",erp);
							
							var dsrv = '';
							dsrv = 'editdialogservice';
			
							var srv = registry.byId(dsrv);
							srv.set("value",svcname);
			
							var dsrvt = '';
							dsrvt = 'editdialogservicetype';
			
							var srvtype = registry.byId(dsrvt);
							srvtype.set("value",svctype);
			
							var dsrvti = '';
							dsrvti = 'editdialogsvcid';
			
							var servid = registry.byId(dsrvti);
							servid.set("value",svcid);
			
							var dpr = '';
							dpr = 'editdialogprice';
			
							var pr = dom.byId(dpr);
							pr.value =formatCurrency(svccost);
							
							var dno = '';
							dno = 'editdialognotes';
			
							var dnotes = dom.byId(dno);
							dnotes.value =notes;								
						}
					}
				}
			}
			function doAjax(id,whichdlg) {
				if(whichdlg=='editApptDialog'){
					var donotdoget = true;
					if(donotdoget){
						StillNeedsValidating=false;
						noajax(id);
						standby.hide();
					}else{
						fillServices(id);
						var deferred = dojo.xhrGet( {
							url : "myschedule/appt?apptid="+id,
							handleAs: "text",
							preventCache: true,
							load : function(response, newValue) {
								registry.byId('editApptDialog').reset();
								registry.byId('progButton').set('label','Select one');registry.byId('progButton').set('value','');
								// handle response codes
								// do something with returned data
								// console.log("the appointment
								// object returned: "+response);
								 var isJSON;
								 var data;
									try {
										data = JSON.parse(response);
										isJSON = true;
									}
									catch (e) {
										isJSON = false;
									}
	
									if (isJSON) {
										// data is already parsed,
										// so just use it
										// handle response codes
										// do something with
										// returned data
										
										if(data.constructor == Array){
										
											for(var key in data) {
												var status = data[key].status;
												var firstname =  "";
												var lastname = "";
												if(data[key].client != null){
													firstname = data[key].client.firstName;
													lastname = data[key].client.lastName;
													registry.byId("c_editclientname_id").set('value',data[key].client.id);
													registry.byId("c_editclientname_id").setAttribute('disabled', true);
												}
												
												if( status == 'CHECKED_OUT'){
													registry.byId("fedithour").setDisabled(true);
													registry.byId("editminute").setDisabled(true);
													registry.byId("editampm").setDisabled(true);
													registry.byId("editdialogprice").setDisabled(true);
													registry.byId("editdialognotes").setDisabled(true);
													registry.byId("editsavebutton").setDisabled(true);
													registry.byId("editcancelbutton").setDisabled(true);
													registry.byId("editnsbutton").setDisabled(true);
													registry.byId("editcobutton").setDisabled(true);
												}else{
													registry.byId("fedithour").setDisabled(false);
													registry.byId("editminute").setDisabled(false);
													registry.byId("editampm").setDisabled(false);
													registry.byId("editdialogprice").setDisabled(false);
													registry.byId("editdialognotes").setDisabled(false);
													registry.byId("editsavebutton").setDisabled(false);
													registry.byId("editcancelbutton").setDisabled(false);
													registry.byId("editnsbutton").setDisabled(false);
													registry.byId("editcobutton").setDisabled(false);
													
												}
											
												var sbeginetimex = data[key].s_beginDateTime;
	
												var sendetimex = data[key].s_endDateTime;
	
												var iscancelled = false;
												if(data[key].cancelled == null || data[key].cancelled == 'false'){
													iscancelled = false;
												}else{
													iscancelled = data[key].cancelled;
												}
												var apptdate = new Date(data[key].appointmentDate);
												var yearapptdate = apptdate.getFullYear();
												var monthapptdate = apptdate.getMonth()+1;
												var dayapptdate = apptdate.getDate();
	
												var calendar = registry.byId("c_editselectdate");
												console.log('calendar value: '+calendar.value);
												var realdate = new Date(apptdate.getUTCFullYear(),apptdate.getUTCMonth(),apptdate.getUTCDate());
												calendar.set('value',realdate);
												
												var endtime = new Date(data[key].endDateTime.time);
												var endhour = endtime.getHours();
												var endmin = endtime.getMinutes();
												var eap = "am";
												if (endhour   > 11) { eap = "pm";        }
												if (endhour   > 12) { endhour = endhour - 12; }
												if (endhour   == 0) { endhour = 12;        }
												
												var begintime = new Date(data[key].beginDateTime.time);
												var beginhour = begintime.getHours();
												var beginmin = begintime.getMinutes();
												var ap = "am";
												if (beginhour   > 11) { ap = "pm";        }
												if (beginhour   > 12) { beginhour = beginhour - 12; }
												if (beginhour   == 0) { beginhour = 12;        }
	
												var fwidget = registry.byId("fedithour");
												var coln = sbeginetimex.indexOf(':');
												beginhour = sbeginetimex.substring(0,coln);
												if(fwidget){
													
													fwidget.setValue(beginhour);
												}
												
												var editmin = registry.byId("editminute");
												beginmin = sbeginetimex.substring(coln+1,coln+3);
												if(beginmin == 0){
													beginmin = '00';
												}
												if(editmin){
													editmin.setValue(new String(beginmin));
												}
												var editampm = registry.byId("editampm");
												ap = sbeginetimex.substring(sbeginetimex.length,sbeginetimex.length-2);
												if(editampm){
													editampm.setValue(ap.toLowerCase());
												}
	
												// end time
												var fewidget = registry.byId("fendedithour");
												var coln = sendetimex.indexOf(':');
												endhour = sendetimex.substring(0,coln);
												if(fewidget){
													
													fewidget.setValue(endhour);
												}
												
												var endeditmin = registry.byId("endeditminute");
												endmin = sendetimex.substring(coln+1,coln+3);
												if(endmin == 0){
													endmin = '00';
												}
												if(endeditmin){
													endeditmin.setValue(new String(endmin));
												}
												var endeditampm = registry.byId("endeditampm");
												eap = sendetimex.substring(sendetimex.length,sendetimex.length-2);
												if(endeditampm){
													endeditampm.setValue(eap.toLowerCase());
												}
												
												var svccost = "";
												if(data[key].service1cost == null){
													svccost = "0.00";
												}else{
													svccost = data[key].service1cost;
												}
												
												id = data[key].id;
												if(data[key].servicename1 == null){
												}else{
													svcname = data[key].servicename1;
												}
												
												var svctype = "";
												if(data[key].service1type == null){
												}else{
													svctype = data[key].service1type;
												}
												
												
												if(data[key].service1id == null){
												}else{
													svcid = data[key].service1id;
												}
												
												var dsrvselect = '';
												dsrvselect = 'editselect';
	
												var stateStore = null;
												id = id.substring(id.length-2,id.length);
												
												var id = "0";
												id = data[key].id;
												
												var notes = "";
												if(data[key].notes == null){
												}else{
													notes = unescape(data[key].notes);
												}
	
												
												
												var dapptid = '';
												dapptid = 'editdialogapptid';
	
												var d = registry.byId(dapptid);
												d.set("value",id);
												
												var erp = "0";
												erp = data[key].recur_parent;
												var e_r_p = registry.byId('edit_recur_parent');
												e_r_p.set("value",erp);
												
												var dsrv = '';
												dsrv = 'editdialogservice';
	
												var srv = registry.byId(dsrv);
												srv.set("value",svcname);
	
												var dsrvt = '';
												dsrvt = 'editdialogservicetype';
	
												var srvtype = registry.byId(dsrvt);
												srvtype.set("value",svctype);
	
												var dsrvti = '';
												dsrvti = 'editdialogsvcid';
	
												var servid = registry.byId(dsrvti);
												servid.set("value",svcid);
	
												var dpr = '';
												dpr = 'editdialogprice';
	
												var pr = dom.byId(dpr);
												pr.value =formatCurrency(svccost);
												
												var dno = '';
												dno = 'editdialognotes';
	
												var dnotes = dom.byId(dno);
												dnotes.value =notes;
											}
											
										}else{
												sometext = data[0].description;
												console.log(sometext);
										}
									}
									else {
										// try treating it as XML
									}		
								standbydlg.hide();													
								return response;
							},
							error: function(error) {
										console.log("An unexpected error occurred: " + error);
										standbydlg.hide();
										window.location.reload();
									}													
						});		
					}
					
					// clearDialog();
					var dlg = registry.byId(whichdlg);
					dlg.show();
				}else{
				// personal time
					displayPersonalDialog(id,true);

				}					
			}
			
			function formatCurrency(num) {
				num = isNaN(num) || num === '' || num === null ? 0.00 : num;
				return parseFloat(num).toFixed(2);
			}
			function remove(id)
			{
				console.log("ENTERED remove() id: "+id);
				return (elem=document.getElementById(id)).parentNode.removeChild(elem);
			}
			function deletePersonal(){
				standby.show();
				var id = registry.byId("editpersonalapptid").get("value");
				var isthisagendaviewflagval = "false"; 
				isthisagendaviewflagval = registry.byId("isThisAgendaViewFlag").get("value");
				console.log("delete editpersonalapptid id : "+id);
	            dojo.xhrGet(
	            		{
			                url: "myschedule/delete?apptid="+id,
			                preventCache: true,
			                handle: function(data) {
			                	console.log("SUCCESS FROM DELETE PERSONAL");
								try{
									console.log("before entering displayEditTodaysAgenda deleteappointment ");
									var rawdate = registry.byId("c_editselectdate").attr("value");
									console.log("c_editselectdate: "+rawdate);
									var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
									if(isthisagendaviewflagval == "false"){
										displayEditTodaysAgenda(postdate);
									}else{
										console.log("UPDATE THE AGENDA VIEW FOR THE DELETED EVENT");
										loadAgenda1();
									}
									standby.hide();
								}catch(err){
									console.log("deleteappointment could not display todays agenda error message :\n "+err);
									standby.hide();
								}
			                },
			                error: function(error) {
			                	console.log("ERROR FROM DELETE PERSONAL");
			                    console.log(error);
			                    window.location.reload();
			                }
			            }	            		
	            
	            );							
			}
			
			function deleteAppointment(dialogFields){
				standby.show();
				var id = registry.byId("editdialogapptid").attr("value");
				var erp = registry.byId("edit_recur_parent").attr("value");
				var deletefuture = "false";
				if(erp != ''){
					var x = confirm("Do you wish to delete future appointments too?");
					if (x){
						deletefuture = "true";
					}else{
						deletefuture = "false";
					}
				}
				var isThisEditAgendaViewFlag = "false";
				isThisEditAgendaViewFlag = registry.byId("isThisEditAgendaViewFlag").attr("value");
				console.log("delete editdialogapptid id : "+id);
	            dojo.xhrGet(
	            		{
			                url: "myschedule/delete?apptid="+id+"&amp;df="+deletefuture,
			                preventCache: true,
			                handle: function(data) {
			                	console.log("SUCCESS FROM DELETE APPOINTMENT");
								try{
									console.log("before entering displayEditTodaysAgenda deleteappointment ");
									var rawdate = registry.byId("c_editselectdate").attr("value");
									console.log("c_editselectdate: "+rawdate);
									var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
									if(isThisEditAgendaViewFlag == "false"){
										displayEditTodaysAgenda(postdate);	
									}else{
										loadAgenda1();
									}
									standby.hide();
								}catch(err){
									console.log("deleteappointment could not display todays agenda error message :\n "+err);
									standby.hide();
								}
			                },
			                error: function(error) {
			                	console.log("ERROR FROM DELETE APPOINTMENT");
			                    console.log(error);
			                    window.location.reload();
			                }
			            }	            		
	            
	            );				
			}
			
			function cancelAppointment(){
				var newDate = document.getElementById("hEditSelectDate").value;
				var isThisEditAgendaViewFlag = "false";
				isThisEditAgendaViewFlag = registry.byId("isThisEditAgendaViewFlag").attr("value");
				standby.show();
				var id = registry.byId("editdialogapptid").attr("value");
				console.log("cancel editdialogapptid id : "+id);
	            // Call the asynchronous xhrPost
	            dojo.xhrGet(
	            		{
			                url: "myschedule/cancel?apptid="+id,
							handleAs: "text",
							preventCache: true,
			                load: function(data) {
			                	console.log("SUCCESS FROM CANCEL APPOINTMENT");
								try{
									console.log("before entering displayEditTodaysAgenda cancel appointment ");
									var rawdate = registry.byId("c_editselectdate").attr("value")
									console.log("c_editselectdate: "+rawdate);
									var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
									if(isThisEditAgendaViewFlag == "false"){
										displayEditTodaysAgenda(postdate);
									}else{
										loadAgenda1();
									}
									standby.hide();
								}catch(err){
									console.log("cancel appointment could not display todays agenda error message :\n "+err);
									standby.hide();
								}
			                },
			                error: function(error) {
			                	console.log("ERROR FROM CANCEL APPOINTMENT");
			                    console.log(error);
			                    window.location.reload();
			                }
			            }	            		
	            
	            );				
				
			}
			function noShowAppointment(dialogFields){
				standby.show();
				var id = registry.byId("editdialogapptid").attr("value");
				var isThisEditAgendaViewFlag = "false";
				isThisEditAgendaViewFlag = registry.byId("isThisEditAgendaViewFlag").attr("value");
				console.log("noshow editdialogapptid id : "+id);
	            dojo.xhrGet(
	            		{
			                url: "myschedule/noshow?apptid="+id,
			                preventCache: true,
			                handle: function(data) {
			                	console.log("SUCCESS FROM NOSHOW APPOINTMENT");
								try{
									console.log("before entering displayEditTodaysAgenda noshow appointment ");
									var rawdate = registry.byId("c_editselectdate").attr("value")
									console.log("c_editselectdate: "+rawdate);
									var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
									if(isThisEditAgendaViewFlag == "false"){
										displayEditTodaysAgenda(postdate);	
									}else{
										loadAgenda1();
										
									}
									standby.hide();
								}catch(err){
									console.log("noshow could not display todays agenda error message :\n "+err);
									standby.hide();
								}
			                },
			                error: function(error) {
			                	console.log("ERROR FROM NOSHOW APPOINTMENT");
			                    console.log(error);
			                    window.location.reload();
			                }
			            }	            		
	            
	            );					
			}
			function saveAndCheckout(){
				var rawdate = registry.byId("c_editselectdate").attr("value")
				var nowdate = new Date();
				nowdate.setHours(0);
				nowdate.setSeconds(0);
				nowdate.setMinutes(0);
				nowdate.setMilliseconds(0);
				
				rawdate.setHours(0);
				rawdate.setSeconds(0);
				rawdate.setMinutes(0);
				rawdate.setMilliseconds(0);
				var dothis = false;
				if(rawdate.valueOf() != nowdate.valueOf()){
					var conf = confirm("The Checkout date is different from todays date. Are you sure you want to Check Out?");

				    if(conf == true){
				    	console.log("dothis is true");
				    	dothis = true;
				    }else{
				    	console.log("dothis is false");
				    }					
				}else{
					console.log("dothis is true");
					dothis = true;
				}
				if(dothis){
					var nextdeferred = new dojo.Deferred();
					updateAppointment(nextdeferred);
					nextdeferred.then(function(res){
							checkOutAppointment(res);
							
						},function(err){
							// This will be called when the deferred
							// is rejected
							console.log("ERROR " + err);
					}
					);
				}
			}
			function checkOutAppointment(res){
				standby.show();
				 
				var isJSON;
				 var id = res;
				 var isThisEditAgendaViewFlag = "false";
				 isThisEditAgendaViewFlag = registry.byId("isThisEditAgendaViewFlag").attr("value");
				 // console.log("the appointment object returned:
					// "+res);
				 var data;
					try {
						data = JSON.parse(res);
						isJSON = true;
					}
					catch (e) {
						isJSON = false;
					}
					if (isJSON) {
						if(data.constructor == Array){
							for(var key in data) {
								id = data[key].id;

							}
						}
					}			
					console.log("checkout editdialogapptid id : "+id);
					dojo.xhrGet(
							{
								url: "myschedule/checkout?apptid="+id,
								preventCache: true,
								handle: function(data) {
									console.log("SUCCESS FROM NOSHOW APPOINTMENT");
									try{
										console.log("before entering displayEditTodaysAgenda checkout appointment ");
										var rawdate = registry.byId("c_editselectdate").attr("value")
										console.log("c_editselectdate: "+rawdate);
										var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
										if(isThisEditAgendaViewFlag == "false"){
											displayEditTodaysAgenda(postdate);	
										}else{
											loadAgenda1();
										}
										standby.hide();
									}catch(err){
										console.log("checkout could not display todays agenda error message :\n "+err);
										standby.hide();
									}
								},
								error: function(error) {
									console.log("ERROR FROM NOSHOW APPOINTMENT");
									console.log(error);
									window.location.reload();
								}
							}	            		
					
					);					
			}
	function displayEditTodaysAgenda(newValue){
		console.log("ENTERED displayEditTodaysAgenda");
		var dayte = "";
		var agendadate = "";
		try{
			console.log("newValue: "+newValue);
			dayte = dojo.date.locale.format(newValue, {datePattern: "yyyy-MM-dd", selector: "date"});
			agendadate = dojo.date.locale.format(newValue, {datePattern: "MM-dd-yyyy", selector: "date"});
		}catch(err){
			console.log("could not format the newValue date: "+ err);
			dayte = newValue;
			agendadate = newValue;
		}
		document.getElementById("hSelectDate").value=dayte;
		console.log("displayEditTodaysAgenda dayte: "+dayte);
		var deferred = dojo.xhrGet( {
							url : "myschedule/agenda?date="+document.getElementById("hSelectDate").value,
							preventCache: true,
							handle : function(response, newValue) {
								// handle response codes
								// do something with returned data
								console.log("the appointment object returned: "+response);
								 var isJSON;
								 var data;
									try {
										data = JSON.parse(response);
										isJSON = true;
									}
									catch (e) {
										isJSON = false;
									}

									if (isJSON) {
										// data is already parsed,
										// so just use it
										// handle response codes
										// do something with
										// returned data
										
										var mytable = document.getElementById("agenda");
										console.log("past get id of agenda");
										
										var oldagendaheader = document.getElementById("agendadate");
										oldagendaheader.innerHTML = "Agenda for "+agendadate;
										
										
										
										for(var i = document.getElementById("agenda").rows.length; i > 1;i--)
										 {
											document.getElementById("agenda").deleteRow(i -1);
										 }
										var mycurrent_row,mycurrent_cell;
										mytable = document.getElementById("agenda");
										if(data.constructor == Array){
											appointmentsArray = [];
											customServicesPricesArray = [];
											for(var key in data) {
												if(global_selected_staff != ''){
													if(data[key].staff.id != global_selected_staff){
														continue;
													}
												}
												try{
												var sbeginetimex = data[key].s_beginDateTime;
												var sendetimex = data[key].s_endDateTime;
												console.log("in for loop");

												var myTime = new Date(data[key].beginDateTime.timeInMillis);
												var myEndTime = new Date(data[key].endDateTime.timeInMillis);
												var endhh = myEndTime.getHours();
												var endmm = myEndTime.getMinutes();
												
												
												var hh = myTime.getHours();
												var mm = myTime.getMinutes();
												var ss = myTime.getSeconds();
												var ms = myTime.getMilliseconds();																
												var ap = "am";
												var endap = "am";
												
												if (hh   > 11) { ap = "pm";        }
												if (hh   > 12) { hh = hh - 12; }
												if (hh   == 0) { hh = 12;        }
												
												if (endhh   > 11) { endap = "pm";        }
												if (endhh   > 12) { endhh = endhh - 12; }
												if (endhh   == 0) { endhh = 12;        }
												
												var timetext = "Begin: " + hh + ":" + mm + " " + ap;
												var endtimetext = "End: " + endhh + ":" + zeroFill(endmm,2) + " " + endap;
											
											
											
												mycurrent_row = document.createElement("tr");
												
												mycurrent_row.setAttribute("id","appointment"+data[key].id);
												mycurrent_row.setAttribute("onMouseOver","this.className='highlight'");
												mycurrent_row.setAttribute("onMouseOut","this.className='normal'");
												mycurrent_cell = document.createElement("td");
												var svc = data[key].servicename1 + " $" + formatCurrency(data[key].service1cost);
												if(data[key].client != null){
													mycurrent_row.setAttribute("status",data[key].status);
													mycurrent_row.setAttribute("onClick","doAjax("+data[key].id+",'editApptDialog');");
													sometext = "STATUS: "+data[key].status +" "+data[key].client.firstName + " " + data[key].client.lastName + " "+sbeginetimex + " " + sendetimex + " " + svc;
												}else{
													mycurrent_row.setAttribute("status","PERSONAL");
													mycurrent_row.setAttribute("onClick","doAjax("+data[key].id+",'personalDialog');");
													sometext = "STATUS: "+data[key].status +" TIME BLOCK  "+data[key].personallabel + " " +sbeginetimex + " " + sendetimex + " ";
												}
												var datatext = document.createTextNode(" " + sometext);																
												console.log("before data.id");
												mycurrent_cell.setAttribute("id","appointment"+data[key].id);
												console.log("after data.id");
												mycurrent_cell.appendChild(datatext);												
												mycurrent_row.appendChild(mycurrent_cell);
												var tbody = mytable.getElementsByTagName('tbody')[0];
												tbody.appendChild(mycurrent_row);
													mytable.appendChild(tbody);		
													var agendaobject = data[key];			
													var clientobject = agendaobject.client;
													if(clientobject){
														console.log("before data.id 2 "+clientobject);
														var getappt = new appointmentobject(data[key].staff.id,""+agendaobject.status,""+agendaobject.id,agendaobject.appointmentDate,""+agendaobject.service1id,""+agendaobject.servicename1,""+agendaobject.service1cost,""+agendaobject.client.id,""+agendaobject.client.firstName,""+agendaobject.client.lastName,""+agendaobject.notes,""+hh,""+mm,""+ap,""+endhh,""+zeroFill(endmm,2),""+endap,data[key].recur_parent,data[key].requested_image_path);
														console.log("after data.id 2");												
														appointmentsArray.push(getappt);	
														console.log("after append child");
														var custserviceprice = new customservicepriceobject(agendaobject.id,agendaobject.service1id,agendaobject.service1cost);
														customServicesPricesArray.push(custserviceprice);
														
													}else{
														console.log("before data.id 3 "+clientobject);
														var getappt = new appointmentobject(data[key].staff.id,""+agendaobject.status,""+agendaobject.id,agendaobject.appointmentDate,""+agendaobject.service1id,""+agendaobject.servicename1,""+agendaobject.service1cost,"","","",""+agendaobject.notes,""+hh,""+mm,""+ap,""+endhh,""+zeroFill(endmm,2),""+endap,data[key].recur_parent);
														console.log("after data.id 3");												
														appointmentsArray.push(getappt);	
														console.log("after append child");
														var custserviceprice = new customservicepriceobject(agendaobject.id,agendaobject.service1id,agendaobject.service1cost);
														customServicesPricesArray.push(custserviceprice);
														
													}
												}catch(err){
													console.log(err);
												}
											}
											
										}else{
												sometext = data[0].description;
												alert(sometext);
										
										}
									}
									else {
										// try treating it as XML
									}		
								console.log("about to hide the standby");
								standby.hide();
								return response;
							}
						});	
	}
			function fancy_confirm(title, message, onYes, onNo) {
				  registry.byId('id_dialog_button_1').attr("style", "color:crimson;font-weight:bold");
				  registry.byId('id_dialog_button_1').attr("label", "Yes, Delete");
				  var p = registry.byId('id_dialog');
				  p.attr( "title", title );
				  dom.byId('id_dialog_text').innerHTML = message;
				  p.execute = dojo.hitch( p, function() {
					if( dojo.isObject( arguments ) ) {
					  onYes();
					} else {
					  onNo();
					}
				  });
				  p.show();
			}
			function saveAddDialog(){
				standby.show();
				var firstname = registry.byId('firstname_dlg').attr("value");
				firstname = toTitleCase(firstname);
				var lastname = registry.byId('lastname_dlg').attr("value");
				lastname = toTitleCase(lastname);
				var phonenumber = registry.byId('home_phonenumber_dlg').attr("value");

				var dob = '';
				var address1 = ''; 
				var address2 = ''; 
				var city = ''; 
				var state = ''; 
				var zip = ''; 
				var homephone = "";
				if(phonenumber != null){
					homephone = phonenumber;
				}
 
				var workphone = ''; 
				var cellphone = ''; 
				var email = ''; 
				dojo.xhrGet({
				  url: "clients/addclient?f="+firstname+"&amp;l="+lastname+"&amp;dob="+dob+"&amp;a1="+address1+"&amp;a2="+address2+"&amp;c="+city+"&amp;s="+state+"&amp;z="+zip+"&amp;wp="+workphone+"&amp;hp="+homephone+"&amp;cp="+cellphone+"&amp;e="+email,
				  handleAs: "json",
				  timeout: 5000,
				  preventCache: true,
				  load: function(response, ioArgs) {
					if(response == "SUCCESS"){
						loadClientsList();
						var widg = document.getElementById('upgraderequired');
						if(widg){
							widg.style.display = 'none';
						}
					}else if(response == "FAILURE"){
						var widg = document.getElementById('upgraderequired');
						if(widg){
							widg.style.display = 'inline';
						}
					}else{
						alert("First name and last name already exists.");
					}
					standby.hide();
					return response;
				  },
				  error: function(response, ioArgs) {
					console.log("HTTP status code: " + ioArgs.xhr.status);
					standby.hide();
					window.location.reload();
					return response;
				  }
				});	 
			}			
			function addnewclient(){
				try{
					var dlg = registry.byId('addClientDialog');
					dlg.reset();
					dlg.show();
				}catch(e){
					console.log(e);
				}
			}	
	 function isAlive() {
		dojo.xhrPost(
				{
					url: "myschedule/isalive",
					content: {
					  },
					handleAs: "json",
					load: function(data) {
						console.log("SUCCESS FROM CHECKING alive");
						console.log("IS THERE AN APPOINTMENT CONFLICT? "+data);
						if(data == "true"){
							console.log("still alive");
						}
					},
					error: function(error) {
						console.log("NOT alive");
						console.log("error: "+error);
						window.location.reload();
					}
				}	            		
		
		);		
	 }
	function addAppointmentForFC(nextdeferredcheckout){
		console.log("SAVING APPOINTMENT FROM FORM");
		standby = new dojox.widget.Standby({
		  target: "myTabContainer"
		});
		document.body.appendChild(standby.domNode);
		standby.show();
		
		// beginning of update appointment code : getting values
		// from dialog
		var staff_edit_id = registry.byId("fapptstaff_edit").attr("value");
		var client_add_id = registry.byId("c_editclientname_id").attr("value");
		var edithourval = "";
		var editminval = "";
		var editampmval = "";
		var endedithourval = "";
		var endeditminval = "";
		var endeditampmval = "";
		var c_editselectdateval = "";
		var c_raw_editselectdateval = "";
		var svcid = "";
		var newsvcid = "";
		var svctype = "";
		var cancel = "false";
		var svcprice = "";
		var notes = "";
		var whichtable = "agenda";
		var whichtabledate = "agendadate";
		var whichselectdate = "hSelectDate";
		var whichdialog="editApptDialog";

		newsvcid = registry.byId("editselect").attr("value");
		svcid = registry.byId("editdialogsvcid").attr("value");
		if(newsvcid != svcid){
			svcid = newsvcid;
		}
		svctype = registry.byId("editdialogservicetype").attr("value");
		svcprice = formatCurrency(registry.byId("editdialogprice").attr("value"));
		notes = registry.byId("editdialognotes").attr("value");
		edithourval = registry.byId("fedithour").get("value");
		editminval = registry.byId("editminute").get("value");
		editampmval = registry.byId("editampm").get("value");
		endedithourval = registry.byId("fendedithour").get("value");
		endeditminval = registry.byId("endeditminute").get("value");
		endeditampmval = registry.byId("endeditampm").get("value");
		c_editselectdateval = registry.byId("c_editselectdate").attr("value");
		c_raw_editselectdateval = registry.byId("c_editselectdate").attr("value");
		c_editselectdateval = dojo.date.locale.format(c_editselectdateval, {datePattern: "yyyy-MM-dd", selector: "date"});
		notes = escape(notes);		
		// end of update appointment code : getting values from
		// dialog
		
		var rawdate = c_raw_editselectdateval;
		var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
		
		var rawstaffid = staff_edit_id;
		console.log("fapptstaff: "+rawstaffid);
		if(!rawstaffid){
			alert("Please select a staff");
			registry.byId("fapptstaff_edit").focus();
			standby.hide();
			return false;
		}
		var rawcid = client_add_id;
		if(!rawcid){
			alert("Please select a client");
			registry.byId("c_editclientname_id").focus();
			standby.hide();
			return false;
		}
		var rawsvcid = svcid;
		if(!rawsvcid){
			alert("Please select a service");
			registry.byId("editdialogsvcid").focus();
			standby.hide();
			return false;
		}
		var rawhour = edithourval;
		if(!rawhour){
			alert("Please select a begin hour");
			registry.byId("fedithour").focus();
			standby.hide();
			return false;
		}
		
		var rawminute = editminval;
		if(!rawminute){
			alert("Please select a begin minute");
			registry.byId("editminute").focus();
			standby.hide();
			return false;
		}
		
		var rawampm = editampmval;
		if(!rawampm){
			alert("Please select a begin am / pm");
			registry.byId("editampm").focus();
			standby.hide();
			return false;
		}
		
		var rawnotes = notes;
		
		var rawehour = endedithourval;
		var raweminute = endeditminval;
		var raweampm = endeditampmval;

		var deferred2 = dojo.xhrPost(
				{
					url: "myschedule/checkdatetimestaff",
					content: {
						staff: rawstaffid,
						hSelectDate: postdate,
						hour: rawhour,
						minute: rawminute,
						ampm: rawampm,
						ehour: rawehour,
						eminute: raweminute,
						eampm: raweampm
					  },
					handleAs: "json",
					load: function(data) {
						standby.hide();
						if(data == "true"){
							console.log("THERE IS AN APPOINTMENT CONFLICT. DO YOU WISH TO BOOK APPOINTMENT ANYWAY?");
						}
					},
					error: function(error) {
						console.log("ERROR FROM CHECKING APPOINTMENT");
						console.log(error);
						standby.hide();
						window.location.reload();
					}
				}	            		
		
		);		
		var nextdeferred = deferred2.then(function(res){
				var scheduleanyways = "true";
				var deferredyn = new dojo.Deferred();
				if(res == "true"){

					var x = confirm("Requested appointment overlaps an existing one.  Do you want to schedule appointment anyway?");
					if (x){
						return "true";
					}else{
						return "false";
					}
				}else{
					res = "true";
				}
				return res;
				
			},function(err){
				// This will be called when the deferred
				// is rejected
				console.log("ERROR " + err);
		}
		);
		nextdeferred.then(function(res){
			if(res == "true"){
				scheduleanyways(nextdeferredcheckout,postdate,rawstaffid, rawcid,rawsvcid,rawhour,rawminute,rawampm,rawnotes,rawehour,raweminute,raweampm);
				
			}else{
				console.log("no scheduling");
			}
		},function(err){
			// This will be called when the deferred
			// is rejected
			console.log("ERROR " + err);
		}
		);
		
		// clear inputs from form
		// registry.byId("apptform").reset();
		// display todays agenda
		
	}	 
	function saveFormAppointment(dialogFields){
		console.log("SAVING APPOINTMENT FROM FORM");
		standby = new dojox.widget.Standby({
		  target: "myTabContainer"
		});
		document.body.appendChild(standby.domNode);
		standby.show();
		var rawdate = registry.byId("c_editselectdate").attr("value")
		console.log("c_editselectdate: "+rawdate);
		var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
		console.log("postdate: "+postdate);
		
		var rawstaffid = registry.byId("fapptstaff").attr("value");
		console.log("fapptstaff: "+rawstaffid);
		if(!rawstaffid){
			alert("Please select a staff");
			registry.byId("fapptstaff").focus();
			standby.hide();
			return false;
		}
		var rawcid = registry.byId("fapptclient").attr("value");
		console.log("fapptclient: "+rawcid);
		if(!rawcid){
			alert("Please select a client");
			registry.byId("fapptclient").focus();
			standby.hide();
			return false;
		}
		var rawsvcid = registry.byId("formapptservice").attr("value");
		console.log("formapptservice: "+rawsvcid);
		if(!rawsvcid){
			alert("Please select a service");
			registry.byId("formapptservice").focus();
			standby.hide();
			return false;
		}
		var rawhour = registry.byId("desiredhour").attr("value");
		console.log("hour: "+ rawhour);
		if(!rawhour){
			alert("Please select a begin hour");
			registry.byId("desiredhour").focus();
			standby.hide();
			return false;
		}
		
		var rawminute = registry.byId("desiredminute").attr("value");
		console.log("minute: "+rawminute);
		if(!rawminute){
			alert("Please select a begin minute");
			registry.byId("desiredminute").focus();
			standby.hide();
			return false;
		}
		
		var rawampm = registry.byId("desiredampm").attr("value");
		console.log("ampm: "+rawampm);
		if(!rawampm){
			alert("Please select a begin am / pm");
			registry.byId("desiredampm").focus();
			standby.hide();
			return false;
		}
		
		var rawnotes = registry.byId("notes").attr("value");
		console.log("notes: "+rawnotes);
		
		var rawehour = registry.byId("desiredehour").attr("value");
		console.log("ehour: "+ rawehour);
		
		var raweminute = registry.byId("desiredeminute").attr("value");
		console.log("eminute: "+raweminute);
		
		var raweampm = registry.byId("desiredeampm").attr("value");
		console.log("eampm: "+raweampm);
		var deferred2 = dojo.xhrPost(
				{
					url: "myschedule/checkdatetimestaff",
					content: {
						staff: rawstaffid,
						hSelectDate: postdate,
						hour: rawhour,
						minute: rawminute,
						ampm: rawampm,
						ehour: rawehour,
						eminute: raweminute,
						eampm: raweampm
					  },
					handleAs: "json",
					load: function(data) {
						console.log("SUCCESS FROM CHECKING APPOINTMENT");
						console.log("IS THERE AN APPOINTMENT CONFLICT? "+data);
						standby.hide();
						if(data == "true"){
							console.log("THERE IS AN APPOINTMENT CONFLICT. DO YOU WISH TO BOOK APPOINTMENT ANYWAY?");
						}
					},
					error: function(error) {
						console.log("ERROR FROM CHECKING APPOINTMENT");
						console.log(error);
						standby.hide();
						window.location.reload();
					}
				}	            		
		
		);		
		var nextdeferred = deferred2.then(function(res){
				var scheduleanyways = "true";
				var deferredyn = new dojo.Deferred();
				if(res == "true"){

					var x = confirm("Requested appointment overlaps an existing one.  Do you want to schedule appointment anyway?");
					if (x){
						return "true";
					}else{
						return "false";
					}
				}else{
					res = "true";
				}
				return res;
				
			},function(err){
				// This will be called when the deferred
				// is rejected
				console.log("ERROR " + err);
		}
		);
		nextdeferred.then(function(res){
			if(res == "true"){
				scheduleanyways(postdate,rawstaffid, rawcid,rawsvcid,rawhour,rawminute,rawampm,rawnotes,rawehour,raweminute,raweampm);
			}else{
				console.log("no scheduling");
			}
		},function(err){
			// This will be called when the deferred
			// is rejected
			console.log("ERROR " + err);
		}
		);
		
		// clear inputs from form
		// registry.byId("apptform").reset();
		// display todays agenda
		
	}
			function scheduleanyways(nextdeferredcheckout,postdate,rawstaffid, rawcid,rawsvcid,rawhour,rawminute,rawampm,rawnotes,rawehour,raweminute,raweampm){
				var isthisagendaviewflagval = "false";
				isthisagendaviewflagval = registry.byId("isThisAgendaViewFlag").get("value");
				
				var er_wdg = registry.byId("enable_recur");
				var enable_recur = er_wdg.attr("checked");
			 	console.log("enable_recur : "+enable_recur);
				var  weekly = registry.byId("weekly").attr("checked");
				console.log("weekly : "+weekly);
		    	var weeklyevery = registry.byId("weeklyevery").attr("checked");
		    	console.log("weeklyevery : "+weeklyevery);
		    	var weeklyeverytext = registry.byId("weeklyeverytext").attr("value");
		    	console.log("weeklyeverytext : "+weeklyeverytext);
		    	var everyweekdaysu = registry.byId("everyweekdaysu").attr("checked");
		    	console.log("everyweekdaysu : "+everyweekdaysu);
		    	var everyweekdaymo = registry.byId("everyweekdaymo").attr("checked");
		    	console.log("everyweekdaymo : "+everyweekdaymo);
		    	var everyweekdaytu = registry.byId("everyweekdaytu").attr("checked");
		    	console.log("everyweekdaytu : "+everyweekdaytu);
		    	var everyweekdaywe = registry.byId("everyweekdaywe").attr("checked");
		    	console.log("everyweekdaywe : "+everyweekdaywe);
		    	var everyweekdaythu = registry.byId("everyweekdaythu").attr("checked");
		    	console.log("everyweekdaythu : "+everyweekdaythu);
		    	var everyweekdayfri = registry.byId("everyweekdayfri").attr("checked");
		    	console.log("everyweekdayfri : "+everyweekdayfri);
		    	var everyweekdaysat = registry.byId("everyweekdaysat").attr("checked");
		    	console.log("everyweekdaysat : "+everyweekdaysat);
		    	var daily = registry.byId("daily").attr("checked");
		    	console.log("daily : "+daily);
		    	var every = registry.byId("every").attr("checked");
		    	console.log("every : "+every);
		    	var everytext = registry.byId("everytext").attr("value");
		    	console.log("everytext : "+everytext);
		    	var everyweekday = registry.byId("everyweekday").attr("checked");
		    	console.log("everyweekday : "+everyweekday);
		    	var monthly = registry.byId("monthly").attr("checked");
		    	console.log("monthly : "+monthly);
		    	var everyday = registry.byId("everyday").attr("checked");
		    	console.log("everyday : "+everyday);
		    	var everymonthdaytext = registry.byId("everymonthdaytext").attr("value");
		    	console.log("everymonthdaytext : "+everymonthdaytext);
		    	var everymonthtext = registry.byId("everymonthtext").attr("value");
		    	console.log("everymonthtext : "+everymonthtext);
		    	var everyregex = registry.byId("everyregex").attr("checked");
		    	console.log("everyregex : "+everyregex);
		    	var everynthdaytext = registry.byId("everynthdaytext").attr("value");
		    	console.log("everynthdaytext : "+everynthdaytext);
		    	var everynthmonthtext = registry.byId("everynthmonthtext").attr("value");
		    	console.log("everynthmonthtext : "+everynthmonthtext);
		    	var everyhowmanymonthtext = registry.byId("everyhowmanymonthtext").attr("value");
		    	console.log("everyhowmanymonthtext : "+everyhowmanymonthtext);
		    	var endafter = registry.byId("endafter").attr("checked");
		    	console.log("endafter : "+endafter);
		    	var endafterxoccur = registry.byId("endafterxoccur").attr("value");
		    	console.log("endafterxoccur : "+endafterxoccur);
		    	var everydate = registry.byId("everydate").attr("checked");
		    	console.log("everydate : "+everydate);
		    	var rangerecurEndDate = registry.byId("rangerecurEndDate").attr("value");
		    	var req_indicator = registry.byId('progButton').attr('value');
		    	
		    	if(rangerecurEndDate != null){
			    	console.log("rangerecurEndDate : "+rangerecurEndDate);
			    	var erdate = dojo.date.locale.format(rangerecurEndDate, {datePattern: "yyyy-MM-dd", selector: "date"})
			    	console.log("erdate : "+erdate);
			    	rangerecurEndDate = erdate;
		    	}
				standby.show();
				// Call the asynchronous xhrPost
				var deferred = dojo.xhrPost(
						{
							url: "myschedule/createappt",
							content: {
								hSelectDate: postdate,
								staffid: rawstaffid,
								clientid: rawcid,
								svcid: rawsvcid,
								hour: rawhour,
								minute: rawminute,
								ampm: rawampm,
								notes: rawnotes,
								ehour: rawehour,
								eminute: raweminute,
								eampm: raweampm,
					    		enable_recur:enable_recur,
					    		weekly:weekly,
					    		weeklyevery:weeklyevery,
					    		weeklyeverytext:weeklyeverytext,
					    		everyweekdaysu:everyweekdaysu,
					    		everyweekdaymo:everyweekdaymo,
					    		everyweekdaytu:everyweekdaytu,
					    		everyweekdaywe:everyweekdaywe,
					    		everyweekdaythu:everyweekdaythu,
					    		everyweekdayfri:everyweekdayfri,
					    		everyweekdaysat:everyweekdaysat,
					    		daily:daily,
					    		every:every,
					    		everytext:everytext,
					    		everyweekday:everyweekday,
					    		monthly:monthly,
					    		everyday:everyday,
					    		everymonthdaytext:everymonthdaytext,
					    		everymonthtext:everymonthtext,
					    		everyregex:everyregex,
					    		everynthdaytext:everynthdaytext,
					    		everynthmonthtext:everynthmonthtext,
					    		everyhowmanymonthtext:everyhowmanymonthtext,
					    		endafter:endafter,
					    		endafterxoccur:endafterxoccur,
					    		everydate:everydate,
					    		rangerecurEndDate:rangerecurEndDate,
					    		ri:req_indicator
							  },
							handleAs: "text",
							load: function(data) {
								console.log("SUCCESS FROM CREATE APPOINTMENT");
								console.log(data);
								try{
									registry.byId("fapptclient").reset();
								}catch(err){
									console.log("could not reset inputs: "+err);
								}
								try{	
									registry.byId("formapptservice").reset();
								}catch(err){
									console.log("could not reset inputs: "+err);
								}
								try{
									registry.byId("desiredhour").set("value","1");
								}catch(err){
									console.log("could not reset inputs: "+err);
								}
								try{
									registry.byId("desiredminute").set("value","00");
								}catch(err){
									console.log("could not reset inputs: "+err);
								}
								try{
									registry.byId("desiredampm").set("value","am");
								}catch(err){
									console.log("could not reset inputs: "+err);
								}
								try{	
									registry.byId("notes").set("value","insert notes here");
								}catch(err){
									console.log("could not reset inputs: "+err);
								}
								try{
									console.log("before entering displayEditTodaysAgenda postdate "+postdate);
									if(isthisagendaviewflagval=='false'){
										displayEditTodaysAgenda(postdate);	
									}else{
										loadAgenda1();
									}
									
								}catch(err){
									console.log("save form appointment could not display todays agenda error message :\n "+err);
									standby.hide();
								}
								<c:if test="${fn:length(staffs) == 1}">
									setOneStaff();
								</c:if>
								<c:if test="${fn:length(staffs) &gt; 1}">
									if(global_selected_staff != ''){
										var staffselectwidget = registry.byId("fapptstaff");		
										staffselectwidget.set("value", global_selected_staff);
									}
								</c:if>
								if(nextdeferredcheckout){
									nextdeferredcheckout.resolve(data);
								}
								standby.hide();
							},
							error: function(error) {
								console.log("ERROR FROM CREATE APPOINTMENT");
								console.log(error);
								window.location.reload();
							}
						}	            		
				
				);			
			}
			function validateSelect(value, constraints)
			{
			    var isValid = false;
			    alert(value);
				console.log("value "+value);
			    return isValid;
			}			
			function savePersonal(dialogFields){
				standby.show();
				var isthisagendaviewflagval = "false";
				var personalstaffval = "";
				var personalhourval = "";
				var personalminval = "";
				var personalampmval = "";
				var personalendhourval = "";
				var personalendminval = "";
				var personalendampmval = "";
				
				var personalreason = "";
				var personalnotes = "";
				
				var c_personalselectdateval = "";
				if(registry.byId('personalDialog').isValid()){
					isthisagendaviewflagval = registry.byId("isThisAgendaViewFlag").get("value");
					
					personalstaffval = registry.byId("fapptstaff_personal").get("value");
					personalhourval = registry.byId("personalhour").get("value");
					personalminval = registry.byId("personalminute").get("value");
					personalampmval = registry.byId("personalampm").get("value");
					personalendhourval = registry.byId("personalendhour").get("value");
					personalendminval = registry.byId("personalendminute").get("value");
					personalendampmval = registry.byId("personalendampm").get("value");
					
					personalreason = registry.byId("personalreason").get("value");
					personalnotes = registry.byId("personalnotes").get("value");
					
					c_personalselectdateval = registry.byId("hPersonalSelectDate").attr("value");
					
					var rawdate = registry.byId("c_personalselectdate").attr("value")
					console.log("c_editselectdate: "+rawdate);
					var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
					console.log("postdate: "+postdate);
					
		            // Call the asynchronous xhrPost
		            var deferred = dojo.xhrPost(
		            		{
				                url: "myschedule/createpersonal",
				                content: {
				                	hSelectDate: postdate,
				                	staff: personalstaffval,
				                	hour: personalhourval,
				                	minute: personalminval,
				                	ampm: personalampmval,
				                	notes: personalnotes,
									label: personalreason,
				                	ehour: personalendhourval,
				                	eminute: personalendminval,
				                	eampm: personalendampmval
				                  },
				                handleAs: "text",
				                load: function(data) {
				                	console.log("SUCCESS FROM CREATE PERSONAL APPOINTMENT");
				                    console.log(data);
									try{
										var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
										console.log("about to enter displayEditTodaysAgenda from updateappointment date: "+postdate);
										if(isthisagendaviewflagval=='false'){
											displayEditTodaysAgenda(postdate);
										}else{
											console.log("THIS IS SUPPOSED TO UPDATE AGENDA VIEW WITH NEW TIME BLOCK");
											loadAgenda1();
										}
									}catch(err){
										console.log("updateappointment could not display todays agenda error message :\n "+err);
										try{
											console.log("before entering displayEditTodaysAgenda rawdate "+rawdate);
											if(isthisagendaviewflagval=='false'){
												displayEditTodaysAgenda(rawdate);	
											}else{
												console.log("THIS IS SUPPOSED TO UPDATE AGENDA VIEW WITH NEW TIME BLOCK");
												loadAgenda1();
											}
											
										}catch(err){
											console.log("updateappointment again could not display todays agenda error message :\n "+err);
											standby.hide();
										}
										
										standby.hide();
									}
				                    
									standby.hide();
				                },
				                error: function(error) {
				                	console.log("ERROR FROM CREATE PERSONAL APPOINTMENT");
				                    console.log(error);
				                    window.location.reload();
				                }
				            }	            		
		            
		            );
				}else{
					alert("Fill out the form.");
				}
			}
			function updatePersonal(dialogFields){
				standby.show();
				var personalhourval = "";
				var personalminval = "";
				var personalampmval = "";
				var personalendhourval = "";
				var personalendminval = "";
				var personalendampmval = "";
				var editpersonalapptid = "";
				var personalreason = "";
				var personalnotes = "";
				var personalstaffval = "";
				var isthisagendaviewflagval = "false";
				
				var c_personalselectdateval = "";
				
				editpersonalapptid = registry.byId("editpersonalapptid").get("value");
				if(editpersonalapptid != ""){
					isthisagendaviewflagval = registry.byId("isThisAgendaViewFlag").get("value");
					console.log("isthisagendaviewflagval: "+isthisagendaviewflagval);
					personalhourval = registry.byId("personalhour").get("value");
					personalstaffval = registry.byId("fapptstaff_personal").get("value");
					personalminval = registry.byId("personalminute").get("value");
					personalampmval = registry.byId("personalampm").get("value");
					personalendhourval = registry.byId("personalendhour").get("value");
					personalendminval = registry.byId("personalendminute").get("value");
					personalendampmval = registry.byId("personalendampm").get("value");
					
					personalreason = registry.byId("personalreason").get("value");
					personalnotes = registry.byId("personalnotes").get("value");
					
					c_personalselectdateval = registry.byId("hPersonalSelectDate").attr("value");
					var rawdate = registry.byId("c_personalselectdate").attr("value");
					var agendadate = registry.byId("c_editselectdate").attr("value");
					console.log("c_personalselectdate: "+rawdate);
					var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
					console.log("postdate: "+postdate);
					
		            // Call the asynchronous xhrPost
		            var deferred = dojo.xhrPost(
		            		{
				                url: "myschedule/updatepersonal",
				                content: {
				                	appointmentid:editpersonalapptid,
				                	staff: personalstaffval,
				                	hSelectDate: postdate,
				                	hour: personalhourval,
				                	minute: personalminval,
				                	ampm: personalampmval,
				                	notes: personalnotes,
									label: personalreason,
				                	ehour: personalendhourval,
				                	eminute: personalendminval,
				                	eampm: personalendampmval
				                  },
				                handleAs: "text",
				                load: function(data) {
				                	console.log("SUCCESS FROM UPDATE PERSONAL APPOINTMENT");
				                    console.log(data);
									try{
										var postdate = dojo.date.locale.format(agendadate, {datePattern: "yyyy-MM-dd", selector: "date"})
										console.log("about to enter displayEditTodaysAgenda from updateappointment date: "+postdate);
										if(isthisagendaviewflagval == "false"){
											displayEditTodaysAgenda(postdate);
										}else{
											console.log("UPDATE THE AGENDA VIEW EVENT");
											loadAgenda1();
										}
									}catch(err){
										console.log("updateappointment could not display todays agenda error message :\n "+err);
										try{
											console.log("before entering displayEditTodaysAgenda rawdate "+agendadate);
											if(isthisagendaviewflagval == "false"){
												displayEditTodaysAgenda(rawdate);
											}else{
												console.log("UPDATE THE AGENDA VIEW EVENT");
												loadAgenda1();
											}
										}catch(err){
											console.log("updateappointment again could not display todays agenda error message :\n "+err);
											standby.hide();
										}
										
										standby.hide();
									}
				                    
									standby.hide();
				                },
				                error: function(error) {
				                	console.log("ERROR FROM UPDATE PERSONAL APPOINTMENT");
				                    console.log(error);
				                    window.location.reload();
				                }
				            }	            		
		            
		            );	
				}else{
		        	alert("This is not a valid Personal Time.");    	
		        }
			}			
			function updateAppointment(nextdeferred){
				var addAppt = registry.byId('isThisADDAgendaViewFlag').get("value");
				if(addAppt == "true"){
					
					if(registry.byId("enable_recur").get("checked")){
						
				    	if(!registry.byId("weekly").get("checked") &amp;&amp;
				    	!registry.byId("weeklyevery").get("checked") &amp;&amp;
				    	!registry.byId("daily").get("checked") &amp;&amp;
				    	!registry.byId("every").get("checked") &amp;&amp;
				    	!registry.byId("everyweekday").get("checked") &amp;&amp;
				    	!registry.byId("monthly").get("checked") &amp;&amp;
				    	!registry.byId("everyday").get("checked") &amp;&amp;
				    	!registry.byId("everyregex").get("checked")){
				    		alert("You need to select at least one option from daily, weekly or monthly.  For Recurring.");
				    		return;
				    	}
				    	if(!registry.byId("endafter").get("checked") &amp;&amp;
				    	!registry.byId("everydate").get("checked")){
				    		alert("You need to select at least one ending type. Either by occurrence or date. For Recurring.");
				    		return;
				    	}
						
						if(registry.byId("everydate").get("checked")){
							var rangerecurEndDate = registry.byId("rangerecurEndDate").attr("value");
							if(rangerecurEndDate==null){
								alert("Please select a end date for recurring.");
								registry.byId("rangerecurEndDate").focus();
								return;
							}
						}
						if(registry.byId("weeklyevery").get("checked")){
					    	var su = registry.byId("everyweekdaysu").get("checked");
					    	var mo = registry.byId("everyweekdaymo").get("checked");
					    	var tu = registry.byId("everyweekdaytu").get("checked");
					    	var we = registry.byId("everyweekdaywe").get("checked");
					    	var th = registry.byId("everyweekdaythu").get("checked");
					    	var fri = registry.byId("everyweekdayfri").get("checked");
					    	var sat = registry.byId("everyweekdaysat").get("checked");
					    	if(!su &amp;&amp; !mo &amp;&amp; !tu &amp;&amp; !we &amp;&amp; !th &amp;&amp; !fri &amp;&amp; !sat){
					    		alert("Please select at least one weekday for recurring.");
					    		return;
					    	}

						}
						if(registry.byId("everyregex").get("checked")){
							var everynthdaytext = registry.byId("everynthdaytext").attr("value");
							var everynthmonthtext = registry.byId("everynthmonthtext").attr("value");
							if(everynthdaytext == 'sel' || everynthmonthtext=='sel'){
					    		alert("Please select from the monthly selection for recurring.");
					    		return;
							}
						}
					}
					
					
					console.log("validate editApptDialog: "+registry.byId('editApptDialog').validate());
					if(registry.byId('editApptDialog').validate()){
						var isthistrueorfalse = addAppointmentForFC(nextdeferred);
						if(isthistrueorfalse != false){
							registry.byId('editApptDialog').hide();	
						}
					}
					
				}else{
					var erp = registry.byId("edit_recur_parent").attr("value");
					var updatefuture = "false";
					if(erp != ''){
						var x = confirm("Do you wish to update future appointments too?");
						if (x){
							updatefuture = "true";
						}else{
							updatefuture = "false";
						}
					}
					
					standby.show();
					
					var isThisEditAgendaViewFlag = "false";
					isThisEditAgendaViewFlag = registry.byId("isThisEditAgendaViewFlag").attr("value");
					
					var id = registry.byId("editdialogapptid").attr("value");
					
					var staff_edit_id = registry.byId("fapptstaff_edit").attr("value");
					var edithourval = "";
					var editminval = "";
					var editampmval = "";
					var endedithourval = "";
					var endeditminval = "";
					var endeditampmval = "";
					var c_editselectdateval = "";
					var c_raw_editselectdateval = "";
					var svcid = "";
					var newsvcid = "";
					var svctype = "";
					var cancel = "false";
					var svcprice = "";
					var notes = "";
					var whichtable = "agenda";
					var whichtabledate = "agendadate";
					var whichselectdate = "hSelectDate";
					var whichdialog="editApptDialog";

					newsvcid = registry.byId("editselect").attr("value");
					console.log('the new service id : '+newsvcid);
					svcid = registry.byId("editdialogsvcid").attr("value");
					if(newsvcid != svcid){
						svcid = newsvcid;
					}
					svctype = registry.byId("editdialogservicetype").attr("value");
					svcprice = formatCurrency(registry.byId("editdialogprice").attr("value"));
					notes = registry.byId("editdialognotes").attr("value");
					edithourval = registry.byId("fedithour").get("value");
					editminval = registry.byId("editminute").get("value");
					editampmval = registry.byId("editampm").get("value");
					endedithourval = registry.byId("fendedithour").get("value");
					endeditminval = registry.byId("endeditminute").get("value");
					endeditampmval = registry.byId("endeditampm").get("value");
					c_editselectdateval = registry.byId("c_editselectdate").attr("value");
					c_raw_editselectdateval = registry.byId("c_editselectdate").attr("value");
					c_editselectdateval = dojo.date.locale.format(c_editselectdateval, {datePattern: "yyyy-MM-dd", selector: "date"});

					var req_indicator = registry.byId('progButton').attr('value');
					
					notes = escape(notes);
					
					var deferred = dojo.xhrGet( {
						url : "myschedule/updateedit?staffid="+staff_edit_id+"&amp;aid="+id+"&amp;c="+cancel+"&amp;sid="+svcid+"&amp;st="+svctype+"&amp;sp="+svcprice+"&amp;n="+notes+"&amp;hour="+edithourval+"&amp;min="+editminval+"&amp;ap="+editampmval+"&amp;d="+c_editselectdateval+"&amp;ehour="+endedithourval+"&amp;emin="+endeditminval+"&amp;eap="+endeditampmval+"&amp;uf="+updatefuture+"&amp;ri="+req_indicator,
						preventCache: true,
						handleAs: "text",
						load : function(response, newValue) {
							
							if(isThisEditAgendaViewFlag == "false"){
								console.log("LOOK AT ME!!!");
								updateCustomPriceForService(svcid,id,svcprice);
							var isJSON;
							 // console.log("the appointment
								// object returned: "+response);
							var data;
							try {
								data = JSON.parse(response);
								isJSON = true;
							}
							catch (e) {
								isJSON = false;
							}
								if (isJSON) {
									if(data.constructor == Array){
										var mytable = document.getElementById(whichtable);
										var oldagendaheader = document.getElementById(whichtabledate);
										var rawdate = document.getElementById("hSelectDate").value;
										if(rawdate == ""){
											rawdate = dojo.date.locale.format(new Date(), {datePattern: "yyyy-MM-dd", selector: "date"});
										}
										oldagendaheader.innerHTML = "Agenda for "+rawdate;
										remove("appointment"+id);
										var mycurrent_row,mycurrent_cell;
										mytable = document.getElementById(whichtable);
									
										for(var key in data) {
											var sbeginetimex = data[key].s_beginDateTime;
											var sendetimex = data[key].s_endDateTime;

											var myTime = new Date(data[key].beginDateTime.timeInMillis);
											var myEndTime = new Date(data[key].endDateTime.timeInMillis);
											var endhh = myEndTime.getHours();
											var endmm = myEndTime.getMinutes();
											
											var hh = myTime.getHours();
											var mm = myTime.getMinutes();
											var ss = myTime.getSeconds();
											var ms = myTime.getMilliseconds();																
											var ap = "am";
											var endap = "am";
											
											if (hh   > 11) { ap = "pm";        }
											if (hh   > 12) { hh = hh - 12; }
											if (hh   == 0) { hh = 12;        }
											
											if (endhh   > 11) { endap = "pm";        }
											if (endhh   > 12) { endhh = endhh - 12; }
											if (endhh   == 0) { endhh = 12;        }
											
											var timetext = "Begin: " + hh + ":" + mm + " " + ap;
											var endtimetext = "End: " + endhh + ":" + endmm + " " + endap;
										
											mycurrent_row = document.createElement("tr");
											
											mycurrent_row.setAttribute("id","appointment"+data[key].id);
											mycurrent_row.setAttribute("onMouseOver","this.className='highlight'");
											mycurrent_row.setAttribute("onMouseOut","this.className='normal'");
											mycurrent_cell = document.createElement("td");
											var svc = data[key].servicename1 + " $" + formatCurrency(data[key].service1cost);
											if(data[key].client != null){
												mycurrent_row.setAttribute("status",data[key].status);
												mycurrent_row.setAttribute("onClick","doAjax("+data[key].id+",'"+whichdialog+"');");
												sometext = "STATUS: "+data[key].status +" "+data[key].client.firstName + " " + data[key].client.lastName + " "+sbeginetimex + " -- " + sendetimex + " " + svc;
											}else{
												mycurrent_row.setAttribute("status","PERSONAL");
												mycurrent_row.setAttribute("onClick","doAjax("+data[key].id+",'personalDialog');");
												sometext = "STATUS: "+data[key].status +" TIME BLOCK "+data[key].personallabel + " "+sbeginetimex + " -- " + sendetimex + " ";
											}
											var datatext = document.createTextNode(" " + sometext);																
											
											mycurrent_cell.appendChild(datatext);												
											mycurrent_row.appendChild(mycurrent_cell);
											mytable.appendChild(mycurrent_row);											
										}
									}else{
											sometext = data[0].description;
											console.log(sometext);
									
									}
									try{
										console.log("1 LOOKING FOR DATE ERROR: rawdate : "+rawdate);
										var postdate = "";
										postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})	
										console.log("about to enter displayEditTodaysAgenda from updateappointment date: "+postdate);
										// displayEditTodaysAgenda(postdate);
										registry.byId("c_editselectdate").set("value",rawdate);
									}catch(err){
										console.log("updateappointment could not display todays agenda error message :\n "+err);
										try{
											console.log("before entering displayEditTodaysAgenda c_editselectdateval "+c_editselectdateval);
											registry.byId("c_editselectdate").set("value",c_raw_editselectdateval);
											// displayEditTodaysAgenda(c_raw_editselectdateval);
										}catch(err){
											console.log("updateappointment again could not display todays agenda error message :\n "+err);
											standby.hide();
										}
										standby.hide();
									}
									registry.byId('editApptDialog').hide();	
									
									registry.byId(whichdialog).reset();
									registry.byId("progButton").set("label","Select one");
									registry.byId("progButton").set("value","");
								}
								else {
									// try treating it as XML
								}	
							}else{
								loadAgenda1();
								registry.byId('editApptDialog').hide();	
								
							}
							if(nextdeferred){
								nextdeferred.resolve(id);
							}
							standby.hide();
							return response;
						},
						error: function(error) {
							console.log("ERROR FROM UPDATE APPOINTMENT");
							console.log(error);
							standby.hide();
							window.location.reload();
						}
					});						
					return deferred;					
				}

			}	
// ***** end middleofpage
	 /*
		 * creates a json object from the data input
		 */
	function convertToJsonObject(response){
		var isJSON;
		var data;
		try {
			// console.log(response);
			data = JSON.parse(response);
			console.log(data);
			isJSON = true;
		}
		catch (e) {
			isJSON = false;
		}

		if (isJSON) {
			// data is already parsed, so just use it
			// handle response codes
			// do something with returned data
			
			if(data instanceof Array){
				
				var cntr = 0;
				var myCars=new Array(); // regular array (add an
										// optional integer
				for(var key in data) {
					bigdata = new Object();
					bigdata.id = data[key].id;
					bigdata.name = data[key].description + " (" + data[key].amounttime +")";
					bigdata.amounttime = data[key].amounttime;
				
					myCars[cntr]=bigdata;       						
					cntr = cntr + 1;
				}
				person = new Object();
				person.items = myCars;
				person.identifier = 'id';
				person.label = 'name';
				return person;
				
			}else{
				console.log(data.id);
				console.log(data.value);
				bigdata = new Object();
				bigdata.id = data.id;
				bigdata.name = data.description;
				
				
				var myCars=new Array(); // regular array (add an
										// optional integer
				myCars[0]=bigdata;       						
				
				person = new Object();
				person.items = myCars;
				person.identifier = 'id';
				person.label = 'name';
				return person;
				
				
			}
		}
	 }				
	 /*
		 * creates a json object from the clients data input
		 */
	function convertClientsToJsonObject(response){
		var isJSON;
		var data;
		try {
			data = JSON.parse(response);
			isJSON = true;
		}
		catch (e) {
			isJSON = false;
		}

		if (isJSON) {
			// data is already parsed, so just use it
			// handle response codes
			// do something with returned data
			
			if(data instanceof Array){
				
				var cntr = 0;
				var myCars=new Array(); // regular array (add an
										// optional integer
				for(var key in data) {
					bigdata = new Object();
					bigdata.id = data[key].id;
					bigdata.name = data[key].firstName + " " + data[key].lastName;
					
					
					myCars[cntr]=bigdata;       						
					cntr = cntr + 1;
				}
				person = new Object();
				person.items = myCars;
				person.identifier = 'id';
				person.label = 'name';
				return person;
				
			}else{
				bigdata = new Object();
				bigdata.id = data.id;
				bigdata.name = data.firstName + " " + data.lastName;
				
				var myCars=new Array(); // regular array (add an
										// optional integer
				myCars[0]=bigdata;       						
				
				person = new Object();
				person.items = myCars;
				person.identifier = 'id';
				person.label = 'name';
				return person;
				
				
			}
		}
	 }		
	 function loadClientsList(){
		 standby.hide();
		 standby.show();
		var deferred = dojo.xhrGet({
			  url: "myschedule/jsonclients",
			  handleAs: "json",
			  preventCache: true,
			  load: function(response) {
					var XstateStore = new Array();
					XstateStore = response;
					var convertedjsonobject = convertClientsToJsonObject(JSON.stringify(XstateStore));
					
					usethisstore = new dojo.data.ItemFileReadStore({
						data: convertedjsonobject
					});
					var isclientlistexists = registry.byId("fapptclient");
					var iseditclientlistexists = registry.byId("c_editclientname_id");

					if(iseditclientlistexists){
						iseditclientlistexists.store = usethisstore;
						
					}else{
						var testeditobject = new registry.form.FilteringSelect({
							store: usethisstore,
							autoComplete: true,
							
							id: "c_editclientname_id"
						},
						"c_editclientname_id");									
						
					}
					
					if(isclientlistexists){
						isclientlistexists.store = usethisstore;
						
					}else{
						var testobject = new registry.form.FilteringSelect({
							store: usethisstore,
							autoComplete: true,
							
							id: "fapptclient"
						},
						"fapptclient");
					}
					standby.hide();
				},
				error: function(error) {
					standby.hide();
					window.location.reload();
				}
		  });
		 
	 }
	 <c:if test="${fn:length(staffs) == 1}">
	 function setOneStaff(){
			
			var staffselectwidget = registry.byId("fapptstaff");
			var personalstaffselectwidget = registry.byId("fapptstaff_personal");
			<c:forEach var="staff" items="${staffs}">
				var staff = '${staff}';
				var staffcoln = staff.indexOf('=');
				var staffid = staff.substring(0,staffcoln);
				personalstaffselectwidget.set("value", staffid);
			</c:forEach>
		
		 
	 }
	 </c:if>
		function zeroFill( number, width )
		{
		  width -= number.toString().length;
		  if ( width > 0 )
		  {
			return new Array( width + (/\./.test( number ) ? 2 : 1) ).join( '0' ) + number;
		  }
		  return number;
		}														
	 
	 function toggleVis(btn,do_show){
			cells = document.getElementsByName('tcol'+btn);
			if(do_show){
				$("#agenda_view_table td:nth-child("+ btn +"),th:nth-child("+ btn +")").show();
				<c:forEach var="staff" items="${staffs}" varStatus="status">
					$('#agendaview${status.count}').fullCalendar('destroy');
					loadAgenda${status.count}();
				</c:forEach>
			}else{
				$("#agenda_view_table td:nth-child("+ btn +"),th:nth-child("+ btn +")").hide();
				<c:forEach var="staff" items="${staffs}" varStatus="status">
					if(btn != ${status.count}){
						$('#agendaview${status.count}').fullCalendar('destroy');
						loadAgenda${status.count}();
					}
				</c:forEach>
	 		}
									
		}				 
// ORIGINAL ADDONLOAD
	 registry.byId("editApptDialog").onShow = function(){editservicecounter=0;};
	 	dojo.connect(registry.byId("editcategories"), "onChange", function(selectedcatid){
	 		console.log('selectedcatid: ' + selectedcatid);
	 		if(selectedcatid == '0'){
				var cntr = 0;
				var myCars=new Array(); // regular array (add an
										// optional integer
				for(var key in servicesArray) {
					var service = servicesArray[key];
					console.log('serviceid: '+service.serviceid);
					console.log('description: '+service.description);
					console.log('amountoftime: '+service.amountoftime);
					
					bigdata = new Object();
					bigdata.value = service.serviceid;
					bigdata.name = service.description + " ("+service.amountoftime+")";
				
					myCars[cntr]=bigdata;       						
					cntr = cntr + 1;
				}
				person = new Object();
				person.items = myCars;
				person.identifier = 'value';
				person.label = 'description';
				var pstore = new dojo.data.ItemFileReadStore({
					   'data':person
				  });
				var cwidget = registry.byId("editselect");
				if(editservicecounter != 0 &amp;&amp; cwidget){
					cwidget.reset();
					cwidget.store = pstore;
				}else{
					editservicecounter = editservicecounter + 1;
				}				 			
	 		}else{
				if(categoryservicesArray.length > 0){
					var cntr = 0;
					var myCars=new Array(); // regular array (add an
											// optional integer
					for(var key in categoryservicesArray) {
						var catservice = categoryservicesArray[key];
						if(selectedcatid == catservice.categoryid){
							console.log('categoryid: '+catservice.categoryid);
							console.log('serviceid: '+catservice.serviceid);
							console.log('description: '+catservice.description);
							console.log('amountoftime: '+catservice.amountoftime);
							
							bigdata = new Object();
							bigdata.value = catservice.serviceid;
							bigdata.name = catservice.description + " ("+catservice.amountoftime+")";
						
							myCars[cntr]=bigdata;       						
							cntr = cntr + 1;
						}
					}
					person = new Object();
					person.items = myCars;
					person.identifier = 'value';
					person.label = 'description';
					var pstore = new dojo.data.ItemFileReadStore({
						   'data':person
					  });
					var cwidget = registry.byId("editselect");
					if(editservicecounter != 0 &amp;&amp; cwidget){
						cwidget.reset();
						cwidget.store = pstore;
					}else{
						editservicecounter = editservicecounter + 1;
					}
					
				}
			}
	 	});
		
			<c:if test="${fn:length(staffs) == 1}">
				setOneStaff();
			</c:if>
/*
 * parser.parse(); var tabs = registry.byId("myTabContainer"); tabs.startup();
 * tabs.resize();
 */						dojo.connect(registry.byId("myTabContainer"), "selectChild", function(page){ 
				if(page.id=='weekTab'){
					// if(week_already_loaded == 'false'){
						loadWeek();
						// week_already_loaded = 'true';
					// }
				} 
				if(page.id=='monthTab'){
					// if(month_already_loaded == 'false'){
						loadMonth();
						// month_already_loaded = 'true';
					// }
				}
				if(page.id=='editTab'){
					<c:forEach var="staff" items="${staffs}" varStatus="status">
						loadAgenda${status.count}();
					</c:forEach>
				}
				if(page.id=='createTab'){
						loadCreate();
				}
				
				
			});
			init();

			dojo.parser.parse();
			<c:if test="${fn:length(staffs) &gt; 1}">
				dojo.connect(registry.byId("staffgeneral"), "onChange", function(staff){ 
					var staffselectwidget = registry.byId("fapptstaff");		
					staffselectwidget.set("value", staff);
					global_selected_staff = staff;
					
					var rawdate = registry.byId("c_editselectdate").attr("value")
					var postdate = dojo.date.locale.format(rawdate, {datePattern: "yyyy-MM-dd", selector: "date"})
					displayEditTodaysAgenda(postdate);
					if ( $('#monthcalendar').children().length > 0 ){
						loadMonth();
					}			
					<c:forEach var="staff" items="${staffs}" varStatus="status">
							if ( $('#agendaview${status.count}').children().length > 0 ){
								loadAgenda${status.count}();
							}								
					</c:forEach>
					
					if ( $('#weekcalendar').children().length > 0 ){
						loadWeek();
					}								
				});
			</c:if>
			var widget = registry.byId("formapptservice");
			dojo.connect(widget, "onChange", function(){
				setEndTime();
			});
			
			dojo.connect(registry.byId('desiredhour'),'onChange',function(value) {
				 console.log("ONCHANGE: desiredhour The begin hour change value is: "+value);
			    // the user has selected a new item
			    // update the end time,
			    // get amount time from service add to begin time
				// set end time
				setEndTime();
			});
			dojo.connect(registry.byId('desiredminute'),'onChange',function(value) {
				 console.log("ONCHANGE: desiredminute The begin minute change value is: "+value);
			    // the user has selected a new item
			    // update the end time,
			    // get amount time from service add to begin time
				// set end time
				setEndTime();
			});
			dojo.connect(registry.byId('desiredampm'),'onChange',function(value) {
				 console.log("ONCHANGE: desiredampm The begin ampm change value is: "+value);
			    // the user has selected a new item
			    // update the end time,
			    // get amount time from service add to begin time
				// set end time
			 	setEndTime();
			});
			// for editapptdialog
			
			dojo.connect(registry.byId('editselect'),'onChange',function(value) {
				 console.log("ONCHANGE: editselect: ");
			    // the user has selected a new item
			    // update the end time,
			    // get amount time from service add to begin time
				// set end time
				setEditEndTime();
			});
			dojo.connect(registry.byId('fedithour'),'onChange',function(value) {
				 console.log("ONCHANGE: fedithour The begin edit hour change value is: "+value);
			    // the user has selected a new item
			    // update the end time,
			    // get amount time from service add to begin time
				// set end time
			    var dothis = registry.byId("isThisEditAgendaViewFlag").attr("value");
			    // if(dothis == 'false'){
			    	setEditEndTime();	
			    // }
				
			});
			dojo.connect(registry.byId('editminute'),'onChange',function(value) {
				 console.log("ONCHANGE: editminute The begin edit minute change value is: "+value);
			    // the user has selected a new item
			    // update the end time,
			    // get amount time from service add to begin time
				// set end time
				    var dothis = registry.byId("isThisEditAgendaViewFlag").attr("value");
				    // if(dothis == 'false'){
				    	setEditEndTime();	
				    // }
			});
			dojo.connect(registry.byId('editampm'),'onChange',function(value) {
				 console.log("ONCHANGE: editampm The begin edit ampm change value is: "+value);
			    // the user has selected a new item
			    // update the end time,
			    // get amount time from service add to begin time
				// set end time
				    var dothis = registry.byId("isThisEditAgendaViewFlag").attr("value");
				    // if(dothis == 'false'){
				    	setEditEndTime();	
				    // }
			});
			dojo.connect(registry.byId("every"), "onChange", function(isChecked){
				if(isChecked){
					registry.byId("everytext").set("disabled",false);	
				}else{
					registry.byId("everytext").set("disabled",true);
				}
			});
			dojo.connect(registry.byId("weeklyevery"), "onChange", function(isChecked){
				if(isChecked){
					registry.byId("weeklyeverytext").set("disabled",false);	
			    	registry.byId("everyweekdaysu").set("disabled",false);
			    	registry.byId("everyweekdaymo").set("disabled",false);
			    	registry.byId("everyweekdaytu").set("disabled",false);
			    	registry.byId("everyweekdaywe").set("disabled",false);
			    	registry.byId("everyweekdaythu").set("disabled",false);
			    	registry.byId("everyweekdayfri").set("disabled",false);
			    	registry.byId("everyweekdaysat").set("disabled",false);
				}else{
					registry.byId("weeklyeverytext").set("disabled",true);
			    	registry.byId("everyweekdaysu").set("disabled",true);
			    	registry.byId("everyweekdaymo").set("disabled",true);
			    	registry.byId("everyweekdaytu").set("disabled",true);
			    	registry.byId("everyweekdaywe").set("disabled",true);
			    	registry.byId("everyweekdaythu").set("disabled",true);
			    	registry.byId("everyweekdayfri").set("disabled",true);
			    	registry.byId("everyweekdaysat").set("disabled",true);
					
				}
			});
			dojo.connect(registry.byId("everyday"), "onChange", function(isChecked){
				if(isChecked){
					registry.byId("everymonthdaytext").set("disabled",false);	
					registry.byId("everymonthtext").set("disabled",false);
				}else{
					registry.byId("everymonthdaytext").set("disabled",true);
					registry.byId("everymonthtext").set("disabled",true);
				}
			});
			dojo.connect(registry.byId("everyregex"), "onChange", function(isChecked){
				if(isChecked){
					registry.byId("everynthdaytext").set("disabled",false);	
					registry.byId("everynthmonthtext").set("disabled",false);
					registry.byId("everyhowmanymonthtext").set("disabled",false);
				}else{
					registry.byId("everynthdaytext").set("disabled",true);
					registry.byId("everynthmonthtext").set("disabled",true);
					registry.byId("everyhowmanymonthtext").set("disabled",true);
				}
			});
			dojo.connect(registry.byId("endafter"), "onChange", function(isChecked){
				if(isChecked){
					registry.byId("endafterxoccur").set("disabled",false);	
				}else{
					registry.byId("endafterxoccur").set("disabled",true);
				}
			});
			dojo.connect(registry.byId("everydate"), "onChange", function(isChecked){
				if(isChecked){
					registry.byId("rangerecurEndDate").set("disabled",false);	
				}else{
					registry.byId("rangerecurEndDate").set("disabled",true);
				}
			});
			dojo.connect(registry.byId("enable_recur"), "onChange", function(isChecked){
			    console.log("entered onchange for enable_recur");
				var container = dojo.query('div','recur_div');
			    if(isChecked){
			    	console.log("entered isChecked. so enable all inputs");
			    	registry.byId("weekly").set("disabled",false);
			    	registry.byId("weeklyevery").set("disabled",false);
			    	registry.byId("daily").set("disabled",false);
			    	registry.byId("every").set("disabled",false);
			    	registry.byId("everyweekday").set("disabled",false);
			    	registry.byId("monthly").set("disabled",false);
			    	registry.byId("everyday").set("disabled",false);
			    	registry.byId("everyregex").set("disabled",false);
			    	registry.byId("endafter").set("disabled",false);
			    	registry.byId("everydate").set("disabled",false);
			    }else{
			    	console.log("entered isChecked false. so disable all inputs");
			    	registry.byId("weekly").set("disabled",true);
			    	registry.byId("weeklyevery").set("disabled",true);
			    	registry.byId("daily").set("disabled",true);
			    	registry.byId("every").set("disabled",true);
			    	registry.byId("everyweekday").set("disabled",true);
			    	registry.byId("monthly").set("disabled",true);
			    	registry.byId("everyday").set("disabled",true);
			    	registry.byId("everyregex").set("disabled",true);
			    	registry.byId("endafter").set("disabled",true);
			    	registry.byId("everydate").set("disabled",true);
			    }
			});
			
			loadAgenda1();
			
            var menu = new Menu({
                style: "display: none;"
            });
          // images/circular icons/star_yellow.png
          
            var button = new DropDownButton({
                label: "Select one",
                name: "programmatic2",
                dropDown: menu,
                id: "progButton"
            });
            dom.byId("dropdownButtonContainer").appendChild(button.domNode);
            parser.parse();
            /*
			 * var menuItem1 = new MenuItem({ label: "Yellow Star", onClick:
			 * function() { registry.byId("progButton").set("label","Yellow
			 * star"); registry.byId("progButton").set("value","yellow"); } });
			 * 
			 * menuItem1.iconNode.style.cssText = "background:
			 * url('images/circular icons/star_yellow.png') no-repeat;";
			 * menu.addChild(menuItem1);
			 * 
			 * var menuItem2 = new MenuItem({ label: "Red star", onClick:
			 * function() { registry.byId("progButton").set("label","Red star");
			 * registry.byId("progButton").set("value","red"); } });
			 * menuItem2.iconNode.style.cssText = "background:
			 * url('images/circular icons/star_red.png') no-repeat;";
			 * 
			 * menu.addChild(menuItem2);
			 */		            	
            var cal = $('#jCalTarget').jCal({
        		day:new Date(),
        		days:1,
        		showMonths:2,
        		monthSelect:true,
        		callback:
        		function (day, days) {
        			console.log('selected ' + days + ' days starting ' + day);
        			jCalOnClick(day);
        		}
        	});
            var clickthisdate = new Date();
            var month = clickthisdate.getMonth();
            month = month + 1;
            var day = clickthisdate.getDate();
            
            var fyear = clickthisdate.getFullYear();
            console.log(''+month+'_'+day+'_'+fyear);
            $('#c2d_'+month+'_'+day+'_'+fyear).click();
            
	 function jCalOnClick(day){
		 jCalTargetValue = day;
		 loadAgenda1(day);
	 }
	function setEditEndTimeNoPost(svcvalue){
		var total = svcvalue;
		var hr = registry.byId('fedithour').value;
		var min = registry.byId('editminute').value;
		var ampm = registry.byId('editampm').value;
		if(hr == 12 ){
			if(ampm == 'am'){
				hr = parseInt('0');
			}else{
				hr = parseInt('12');
			}
		}else if(ampm == 'pm'){
			hr = parseInt(hr) + 12;
		}

		console.log("end hour: "+hr);
		console.log("end min: "+min);
		console.log("end ampm: "+ampm);
		var rawdate = registry.byId("c_editselectdate").attr("value")
		var d = rawdate.getDate();
		var m = rawdate.getMonth();
		var y = rawdate.getFullYear();
		console.log("raw day: "+d);
		console.log("raw month: "+m);
		console.log("raw year: "+y);
		
		var temp_hour_min = total *60;// gives seconds
		var tmphr = Math.floor(temp_hour_min/3600);
		var tmpmn = Math.floor((temp_hour_min%3600)/60);

		var d = new Date(y, m, d, hr, min, 0, 0);
		var newDateObj = new Date(d.getTime() + total*60000);
		
		var endhour = newDateObj.getHours();
		var endmin = newDateObj.getMinutes();
		console.log("setEditEndTimeNoPost revised end hour: "+endhour);
		console.log("setEditEndTimeNoPost revised end min: "+endmin);
		var eap = "am";
		if (endhour   > 11) { eap = "pm";        }
		if (endhour   > 12) { endhour = endhour - 12; }
		if (endhour   == 0) { endhour = 12;        }
		console.log("2 revised end ampm: "+eap);
		var ehr = registry.byId('fendedithour');
		ehr.setValue(endhour);
		var emin = registry.byId('endeditminute');
		if(endmin == "0"){endmin="00";}
		emin.setValue(endmin);
		var eampm = registry.byId('endeditampm');
		eampm.setValue(eap.toLowerCase());				
	}
	function setEditEndTime(){
		if(StillNeedsValidating==false){
			StillNeedsValidating=true;
			return;
		}
		if(registry.byId('editselect')){
			var svcid = registry.byId('editselect').attr("value");
			var srvtime = registry.byId('editselect').attr("displayedValue");
			var dopost = false;
			
			if(svcid != ""){
				if(dopost){
					var deferred = dojo.xhrPost(
							{
								url: "myschedule/svctime",
								content: {
									id: svcid,
								  },
								handleAs: "json",
								load: function(response) {
									console.log("SUCCESS FROM GET SERVICE TIME");
									var total = response.amounttime;
									var hr = registry.byId('fedithour').value;
									var min = registry.byId('editminute').value;
									var ampm = registry.byId('editampm').value;
									if(hr == 12 ){
										if(ampm == 'am'){
											hr = parseInt('0');
										}else{
											hr = parseInt('12');
										}
									}else if(ampm == 'pm'){
										hr = parseInt(hr) + 12;
									}

									console.log("end hour: "+hr);
									console.log("end min: "+min);
									console.log("end ampm: "+ampm);
									var rawdate = registry.byId("c_editselectdate").attr("value")
									var d = rawdate.getDate();
									var m = rawdate.getMonth();
									var y = rawdate.getFullYear();
									console.log("raw day: "+d);
									console.log("raw month: "+m);
									console.log("raw year: "+y);
									
									var temp_hour_min = total *60;// gives
																	// seconds
									var tmphr = Math.floor(temp_hour_min/3600);
									var tmpmn = Math.floor((temp_hour_min%3600)/60);

									var d = new Date(y, m, d, hr, min, 0, 0);
									var newDateObj = new Date(d.getTime() + total*60000);
									
									var endhour = newDateObj.getHours();
									var endmin = newDateObj.getMinutes();
									console.log("setEditEndTime revised end hour: "+endhour);
									console.log("setEditEndTime revised end min: "+endmin);
									var eap = "am";
									if (endhour   > 11) { eap = "pm";        }
									if (endhour   > 12) { endhour = endhour - 12; }
									if (endhour   == 0) { endhour = 12;        }
									console.log("2 revised end ampm: "+eap);
									var ehr = registry.byId('fendedithour');
									ehr.setValue(endhour);
									var emin = registry.byId('endeditminute');
									if(endmin == "0"){endmin="00";}
									emin.setValue(endmin);
									var eampm = registry.byId('endeditampm');
									eampm.setValue(eap.toLowerCase());
								},
								error: function(error) {
									svcstandbydlg.hide();
									console.log("ERROR FROM GET SERVICE TIME");
									console.log(error);
									window.location.reload();
								}
							}	            		
					);
				}else{
					var servicecoln2 = srvtime.indexOf('(');
					var servicecoln3 = srvtime.indexOf(')');
					var servicetime = srvtime.substring(servicecoln2+1,servicecoln3);
				
					setEditEndTimeNoPost(servicetime);
					
                    var editdialogpricewdget = registry.byId('editdialogprice');
                    var editdialogapptidwdget = registry.byId('editdialogapptid');
                    var customprice;
                    if(editdialogapptidwdget != null){
                    	var apptid = editdialogapptidwdget.attr("value")
                    	customprice = getCustomPriceForService(svcid,apptid);
                    	console.log("customprice: "+ customprice);
                    }
                    var price = getPriceForService(svcid);
                    if(customprice != null){
                    	price = customprice; 
                    }
                    console.log("SETTING PRICE FOR SERVICE: "+price);
                    editdialogpricewdget.set("value",formatCurrency(price));
					
				}
			}
		}
	}				
	
	function getCustomPriceForService(svcid,apptid){
		if(customServicesPricesArray.length > 0){
			for(var key in customServicesPricesArray) {
				var findservice = customServicesPricesArray[key];
				if(findservice.serviceid==svcid &amp;&amp; findservice.appointmentid == apptid){
					return findservice.cost;
				}
			}
		}
		
	}
	function updateCustomPriceForService(svcid,apptid,cost){
		if(customServicesPricesArray.length > 0){
			for(var key in customServicesPricesArray) {
				var findservice = customServicesPricesArray[key];
				if(findservice.serviceid==svcid &amp;&amp; findservice.appointmentid == apptid){
					findservice.cost = cost;;
				}
			}
		}
		
	}
	
	function getPriceForService(id){
		if(servicespricesArray.length > 0){
			for(var key in servicespricesArray) {
				var findservice = servicespricesArray[key];
				if(findservice.serviceid==id){
					return findservice.cost;
				}
			}
		}
		
	}
	function setEndTimeNoPost(svcid){
		if(servicesArray.length > 0){
			for(var key in servicesArray) {
				var findservice = servicesArray[key];
				if(findservice.serviceid==svcid){
                    var total = findservice.amountoftime;
                    var hr = registry.byId('desiredhour').value;
                    var min = registry.byId('desiredminute').value;
                    var ampm = registry.byId('desiredampm').value;
                    
					if(hr == 12 ){
						if(ampm == 'am'){
							hr = parseInt('0');
						}else{
							hr = parseInt('12');
						}
					}else if(ampm == 'pm'){
						hr = parseInt(hr) + 12;
					}
					console.log("setEndTimeNoPost end hour: "+hr);
					console.log("setEndTimeNoPost end min: "+min);
					console.log("setEndTimeNoPost end ampm: "+ampm);
                    var rawdate = registry.byId("c_editselectdate").attr("value")
					var d = rawdate.getDate();
					var m = rawdate.getMonth();
					var y = rawdate.getFullYear();
					console.log("setEndTimeNoPost raw day: "+d);
					console.log("setEndTimeNoPost raw month: "+m);
					console.log("setEndTimeNoPost raw year: "+y);
					
					var temp_hour_min = total *60;// gives seconds
					var tmphr = Math.floor(temp_hour_min/3600);
					var tmpmn = Math.floor((temp_hour_min%3600)/60);

                    var d = new Date(y, m, d, hr, min, 0, 0);
                    var newDateObj = new Date(d.getTime() + total*60000);
                    
                    var endhour = newDateObj.getHours();
					var endmin = newDateObj.getMinutes();
					var eap = "am";
					if (endhour   > 11) { eap = "pm";        }
					if (endhour   > 12) { endhour = endhour - 12; }
					if (endhour   == 0) { endhour = 12;        }
					console.log("setEndTimeNoPost revised end hour: "+endhour);
					console.log("setEndTimeNoPost revised end min: "+endmin);
					console.log("1 revised end ampm: "+eap);
                    var ehr = registry.byId('desiredehour');
                    ehr.setValue(endhour);
                    var emin = registry.byId('desiredeminute');
					if(endmin == "0"){endmin="00";}
                    emin.setValue(endmin);
                    var eampm = registry.byId('desiredeampm');
                    eampm.setValue(eap);									
				}
			}
		}
	}
	function setEndTime(){
		var donopost = true;
		var svcid = registry.byId('formapptservice').attr("value");
		if(donopost){
			setEndTimeNoPost(svcid);
		}else{
			standby.show();
            var deferred = dojo.xhrPost(
            		{
		                url: "myschedule/svctime",
		                content: {
		                	id: svcid,
		                  },
		                handleAs: "json",
		                load: function(response) {
		                	console.log("SUCCESS FROM GET SERVICE TIME");
		                    var total = response.amounttime;
		                    var hr = registry.byId('desiredhour').value;
		                    var min = registry.byId('desiredminute').value;
		                    var ampm = registry.byId('desiredampm').value;
							if(hr == 12 ){
								if(ampm == 'am'){
									hr = parseInt('0');
								}else{
									hr = parseInt('12');
								}
							}else if(ampm == 'pm'){
								hr = parseInt(hr) + 12;
							}
							console.log("end hour: "+hr);
							console.log("end min: "+min);
							console.log("end ampm: "+ampm);
		                    var rawdate = registry.byId("c_editselectdate").attr("value")
							var d = rawdate.getDate();
							var m = rawdate.getMonth();
							var y = rawdate.getFullYear();
							console.log("raw day: "+d);
							console.log("raw month: "+m);
							console.log("raw year: "+y);
							
							var temp_hour_min = total *60;// gives
															// seconds
							var tmphr = Math.floor(temp_hour_min/3600);
							var tmpmn = Math.floor((temp_hour_min%3600)/60);

		                    var d = new Date(y, m, d, hr, min, 0, 0);
		                    var newDateObj = new Date(d.getTime() + total*60000);
		                    
		                    var endhour = newDateObj.getHours();
							var endmin = newDateObj.getMinutes();
							var eap = "am";
							if (endhour   > 11) { eap = "pm";        }
							if (endhour   > 12) { endhour = endhour - 12; }
							if (endhour   == 0) { endhour = 12;        }
							console.log("setEndTime revised end hour: "+endhour);
							console.log("setEndTime revised end min: "+endmin);
							console.log("1 revised end ampm: "+eap);
		                    var ehr = registry.byId('desiredehour');
		                    ehr.setValue(endhour);
		                    var emin = registry.byId('desiredeminute');
							if(endmin == "0"){endmin="00";}
		                    emin.setValue(endmin);
		                    var eampm = registry.byId('desiredeampm');
		                    eampm.setValue(eap);
							standby.hide();
		                },
		                error: function(error) {
		                	console.log("ERROR FROM GET SERVICE TIME");
		                    console.log(error);
		                    standby.hide();
		                    window.location.reload();
		                }
		            }	            		
            );
		}
	}
	function displayPersonalDialog(id,edit){
		registry.byId("isThisAgendaViewFlag").set("value","false");
		var newValue = registry.byId("c_editselectdate").attr("value");
		var timeblockwidget = registry.byId("c_personalselectdate");
		console.log("newValue "+newValue);
		var dayte = "";
		try{
			console.log("newValue: "+newValue);
			dayte = dojo.date.locale.format(newValue, {datePattern: "yyyy-MM-dd", selector: "date"});
		}catch(err){
			console.log("could not format the newValue date: "+ err);
			dayte = newValue;
		}
		document.getElementById("hPersonalSelectDate").value=dayte;
		timeblockwidget.set("value",newValue);
		if(edit == true){
			standbydlg = new dojox.widget.Standby({
			  target: "personalDialog"
			});
			document.body.appendChild(standbydlg.domNode);
			standbydlg.show();
		
			$('#updatepersonalbutton').show();
			$('#savepersonalbutton').hide();
			// grab the personl appointment and fill via json and
			// ajax
			
			var deferred = dojo.xhrGet( {
				url : "myschedule/appt?apptid="+id,
				handleAs: "text",
				preventCache: true,
				load : function(response, newValue) {
				
					// handle response codes
					// do something with returned data
					// console.log("the appointment object returned:
					// "+response);
					 var isJSON;
					 var data;
						try {
							data = JSON.parse(response);
							isJSON = true;
						}
						catch (e) {
							isJSON = false;
						}

						if (isJSON) {
							// data is already parsed, so just use
							// it
							// handle response codes
							// do something with returned data
							
							if(data.constructor == Array){
							
								for(var key in data) {
									var status = data[key].status;
									var firstname =  "";
									var lastname = "";
									if(data[key].client != null){
										firstname = data[key].client.firstName;
										lastname = data[key].client.lastName;
									}
								
									var sbeginetimex = data[key].s_beginDateTime;

									var sendetimex = data[key].s_endDateTime;

									var iscancelled = false;
									if(data[key].cancelled == null || data[key].cancelled == 'false'){
										iscancelled = false;
									}else{
										iscancelled = data[key].cancelled;
									}
									var apptdate = new Date(data[key].appointmentDate);
									var yearapptdate = apptdate.getFullYear();
									var monthapptdate = apptdate.getMonth()+1;
									var dayapptdate = apptdate.getDate();

									var endtime = new Date(data[key].endDateTime.time);
									var endhour = endtime.getHours();
									var endmin = endtime.getMinutes();
									var eap = "am";
									if (endhour   > 11) { eap = "pm";        }
									if (endhour   > 12) { endhour = endhour - 12; }
									if (endhour   == 0) { endhour = 12;        }
									
									var begintime = new Date(data[key].beginDateTime.time);
									var beginhour = begintime.getHours();
									var beginmin = begintime.getMinutes();
									var ap = "am";
									if (beginhour   > 11) { ap = "pm";        }
									if (beginhour   > 12) { beginhour = beginhour - 12; }
									if (beginhour   == 0) { beginhour = 12;        }

									var staffforappt = data[key].staff.id;
									var staffwidget = registry.byId("fapptstaff_personal");
									if(staffwidget){
										console.log("STAFF FOR APPT IS: " +staffforappt);
										staffwidget.set("value",staffforappt);
									}
							
									var fwidget = registry.byId("personalhour");
									var coln = sbeginetimex.indexOf(':');
									beginhour = sbeginetimex.substring(0,coln);
									if(fwidget){
										
										fwidget.setValue(beginhour);
									}
									
									var editmin = registry.byId("personalminute");
									beginmin = sbeginetimex.substring(coln+1,coln+3);
									if(beginmin == 0){
										beginmin = '00';
									}
									if(editmin){
										editmin.setValue(new String(beginmin));
									}
									var editampm = registry.byId("personalampm");
									ap = sbeginetimex.substring(sbeginetimex.length,sbeginetimex.length-2);
									if(editampm){
										editampm.setValue(ap.toLowerCase());
									}

									// end time
									var fewidget = registry.byId("personalendhour");
									var coln = sendetimex.indexOf(':');
									endhour = sendetimex.substring(0,coln);
									if(fewidget){
										
										fewidget.setValue(endhour);
									}
									
									var endeditmin = registry.byId("personalendminute");
									endmin = sendetimex.substring(coln+1,coln+3);
									if(endmin == 0){
										endmin = '00';
									}
									if(endeditmin){
										endeditmin.setValue(new String(endmin));
									}
									var endeditampm = registry.byId("personalendampm");
									eap = sendetimex.substring(sendetimex.length,sendetimex.length-2);
									if(endeditampm){
										endeditampm.setValue(eap.toLowerCase());
									}
									
									
									var dsrvselect = '';
									dsrvselect = 'editselect';

									var id = "0";
									id = data[key].id;
									
									var notes = "";
									if(data[key].notes == null){
									}else{
										notes = unescape(data[key].notes);
									}

									var personallabel = "";
									if(data[key].personallabel == null){
									}else{
										personallabel = data[key].personallabel;
									}
									var personreason = '';
									personreason = 'personalreason';
									var prd = registry.byId(personreason);
									prd.set("value",personallabel);
									
									var dapptid = '';
									dapptid = 'editpersonalapptid';

									var d = registry.byId(dapptid);
									d.set("value",id);
									
									var dno = '';
									dno = 'personalnotes';

									var dnotes = dom.byId(dno);
									dnotes.value =notes;
								}
							}else{
									sometext = data[0].description;
									console.log(sometext);
							}
						}
						else {
							// try treating it as XML
						}		
					standbydlg.hide();													
					return response;
				},
				error: function(error) {
							console.log("An unexpected error occurred: " + error);
							standbydlg.hide();
							window.location.reload();
						}													
			});								
			
		}else{
			$('#updatepersonalbutton').hide();
			$('#savepersonalbutton').show();
			registry.byId('personalDialog').reset();
		}
		registry.byId('personaledit').set("value",edit);
		registry.byId('personalDialog').show();
		
	}
	function displayPersonalDialogForFCCalendar(event,edit){
		var id = event.staffid;
		var newValue = event.appointmentdate;
		var status = event.appointmentstatus;
		var timeblockwidget = registry.byId("c_personalselectdate");
		registry.byId("isThisAgendaViewFlag").set("value","true");
		var dayte = "";
		try{
			dayte = dojo.date.locale.format(newValue, {datePattern: "yyyy-MM-dd", selector: "date"});
		}catch(err){
			dayte = newValue;
		}
		document.getElementById("hPersonalSelectDate").value=dayte;
		timeblockwidget.set("value",newValue);
		if(edit == true){
			$('#updatepersonalbutton').show();
			$('#savepersonalbutton').hide();
			var status = event.appointmentstatus;
			var firstname =  "";
			var lastname = "";
			if(event.clientid != ''){
				firstname = event.clientfirstname;
				lastname = event.clientlastname;
			}
		
			var iscancelled = false;
			if(status != 'CANCELED'){
				iscancelled = false;
			}else{
				iscancelled = true;
			}

			var apptdate = event.appointmentdate;
			var yearapptdate = apptdate.getFullYear();
			var monthapptdate = apptdate.getMonth()+1;
			var dayapptdate = apptdate.getDate();

			var endhour = event.endhour;
			var endmin = event.endmin;
			var eap = "am";
			if (endhour   > 11) { eap = "pm";        }
			if (endhour   > 12) { endhour = endhour - 12; }
			if (endhour   == 0) { endhour = 12;        }
			
			var beginhour = event.beginhour;
			var beginmin = event.beginmin;
			var ap = event.beginampm

			var staffforappt = event.staffid;
			var staffwidget = registry.byId("fapptstaff_personal");
			if(staffwidget){
				staffwidget.set("value",staffforappt);
			}
	
			var fwidget = registry.byId("personalhour");
			beginhour = event.beginhour;
			if(fwidget){
				fwidget.setValue(beginhour);
			}
			
			var editmin = registry.byId("personalminute");
			if(editmin){
				editmin.setValue(new String(beginmin));
			}
			var editampm = registry.byId("personalampm");
			ap = event.beginampm;
			if(editampm){
				editampm.setValue(ap.toLowerCase());
			}

			// end time
			var fewidget = registry.byId("personalendhour");
			endhour = event.endhour;
			if(fewidget){
				fewidget.setValue(endhour);
			}
			
			var endeditmin = registry.byId("personalendminute");
			endmin = event.endmin;
			if(endeditmin){
				endeditmin.setValue(new String(endmin));
			}
			var endeditampm = registry.byId("personalendampm");
			eap = event.endampm;
			if(endeditampm){
				endeditampm.setValue(eap.toLowerCase());
			}
			
			
			var dsrvselect = '';
			dsrvselect = 'editselect';

			var id = "0";
			id = event.appointmentid;
			
			var notes = "";
			if(event.appointmentnotes == null){
			}else{
				notes = event.appointmentnotes;
			}

			var personallabel = "";
			if(event.personallabel == null){
			}else{
				personallabel = event.personallabel;
			}
			var personreason = '';
			personreason = 'personalreason';
			var prd = registry.byId(personreason);
			prd.set("value",personallabel);
			
			var dapptid = '';
			dapptid = 'editpersonalapptid';

			var d = registry.byId(dapptid);
			d.set("value",id);
			
			var dno = '';
			dno = 'personalnotes';

			var dnotes = dom.byId(dno);
			dnotes.value =notes;
		}else{
			$('#updatepersonalbutton').hide();
			$('#savepersonalbutton').show();
			registry.byId('personalDialog').reset();
		}
		registry.byId('personaledit').set("value",edit);
		registry.byId('personalDialog').show();
		
	}				
	function init() {
	      standby = new dojox.widget.Standby({
	          target: "myTabContainer"
	      });
	      document.body.appendChild(standby.domNode);
	      // standby.show();
			setInterval(function() {
				isAlive();
			}, 660000); // 11 minutes 660000
	  }				
		/*
		 * define([ 'dojo/_base/lang', 'dojo/_base/declare',
		 * 'dojo/dom-construct', 'dojo/_base/Deferred', 'dijit/Dialog',
		 * 'dijit/form/Button', 'dijit/form/CheckBox' ], function(lang, declare,
		 * construct, Deferred, Dialog, Button, CheckBox) {
		 * 
		 * return declare('snet.DialogConfirm', Dialog, { okButton: null,
		 * cancelButton: null, skipCheckBox: null, hasOkButton: true,
		 * hasCancelButton: true, hasSkipCheckBox: true, hasUnderlay: true, dfd:
		 * null,
		 * 
		 * constructor: function(props) { lang.mixin(this, props); },
		 * 
		 * postCreate: function() { this.inherited('postCreate', arguments);
		 * 
		 * var remember = false; var div = construct.create('div', { className:
		 * 'dialogConfirmButtons' }, this.containerNode, 'last'); if
		 * (this.hasSkipCheckBox) { this.skipCheckBox = new CheckBox({ checked:
		 * false }, construct.create('div'));
		 * div.appendChild(this.skipCheckBox.domNode); var label =
		 * construct.create('label', { 'for': this.skipCheckBox.id, innerHTML:
		 * 'Remember my decision and do not ask again.<br/>' }, div); } if
		 * (this.hasOkButton) { this.okButton = new Button({ label: 'OK',
		 * onClick: lang.hitch(this, function() { remember =
		 * this.hasSkipCheckBox ? this.skipCheckBox.get('checked') : false;
		 * this.hide(); this.dfd.resolve(remember); }) },
		 * construct.create('div')); div.appendChild(this.okButton.domNode); }
		 * if (this.hasCancelButton) { this.cancelButton = new Button({ label:
		 * 'Cancel', onClick: lang.hitch(this, function() { remember =
		 * this.hasSkipCheckBox ? this.skipCheckBox.get('checked') : false;
		 * this.hide(); this.dfd.cancel(remember); }) },
		 * construct.create('div')); div.appendChild(this.cancelButton.domNode); } },
		 * 
		 * show: function() { this.inherited('show', arguments); if
		 * (!this.hasUnderlay) { construct.destroy(this.id + '_underlay'); }
		 * this.dfd = new Deferred(); return this.dfd; } }); });
		 */	
 });
		
 