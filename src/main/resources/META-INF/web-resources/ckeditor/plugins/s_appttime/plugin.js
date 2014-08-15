(function(){
 
	// Section 1 : Code to execute when the toolbar button is pressed
 
	var a= {
	 
	exec:function(editor){
	 
	 editor.insertText('${appttime}');

	 
	}
 
	},
 
	// Section 2 : Create the button and add the functionality to it
	 
	b='s_appttime';
	 
	CKEDITOR.plugins.add(b,{
	 
	init:function(editor){
	 
		editor.addCommand(b,a);
	 
		editor.ui.addButton('s_appttime',{
	 
			label:'Appointment time',
			
			icon: this.path + 'lightbulb.gif',
	
			command:b
	 
		});
		
	}
 
}); 

})();