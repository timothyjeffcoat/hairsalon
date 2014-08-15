(function(){
 
	// Section 1 : Code to execute when the toolbar button is pressed
 
	var a= {
	 
	exec:function(editor){
	 
	 editor.insertText('${stafffirstname}');

	 
	}
 
	},
 
	// Section 2 : Create the button and add the functionality to it
	 
	b='s_stafffirstname';
	 
	CKEDITOR.plugins.add(b,{
	 
	init:function(editor){
	 
		editor.addCommand(b,a);
	 
		editor.ui.addButton('s_stafffirstname',{
	 
			label:'Staff first name',
			
			icon: this.path + 'lightbulb.gif',
	
			command:b
	 
		});
		
	}
 
}); 

})();