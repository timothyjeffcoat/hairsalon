(function(){
 
	// Section 1 : Code to execute when the toolbar button is pressed
 
	var a= {
	 
	exec:function(editor){
	 
	 editor.insertText('${apptserviceprice}');

	 
	}
 
	},
 
	// Section 2 : Create the button and add the functionality to it
	 
	b='s_apptserviceprice';
	 
	CKEDITOR.plugins.add(b,{
	 
	init:function(editor){
	 
		editor.addCommand(b,a);
	 
		editor.ui.addButton('s_apptserviceprice',{
	 
			label:'Appointment service price',
			
			icon: this.path + 'lightbulb.gif',
	
			command:b
	 
		});
		
	}
 
}); 

})();