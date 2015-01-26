function Wait() {
    document.body.style.cursor="wait";
}

function MenuItem_Action( sender, action, name )
{
	if( null!=sender )
	{
		switch( action.toLowerCase() )
		{
			case "leave":
				sender.className=name;
			break;
			
			case "enter":
				sender.className=name;
			break;
			
			default:
	
				alert( "Unknown action: " + action );
			break;

		}
	}
}

function enableFields() {
        if (document.LoginForm.userType[0].checked) {
          document.LoginForm.guestUserName.disabled = false;
          document.LoginForm.guestPassword.disabled = false;
          document.LoginForm.registeredUserName.disabled = true;
          document.LoginForm.registeredUserPassword.disabled = true;
        }
        else {
          document.LoginForm.guestUserName.disabled = true;
          document.LoginForm.guestPassword.disabled = true;
          document.LoginForm.registeredUserName.disabled = false;
          document.LoginForm.registeredUserPassword.disabled = false;
        }
}

function resetFields() {
        document.LoginForm.guestUserName.disabled = false;
        document.LoginForm.guestPassword.disabled = false;
        document.LoginForm.registeredUserName.disabled = true;
        document.LoginForm.registeredUserPassword.disabled = true;
}

function goingInRow(row) {
    row.style.backgroundColor = '#0000FF';
}

function goingOutOfRow(row) {
        row.style.backgroundColor = '#CCCCFF';
}

function setValue(source){
    if(!isAuthanticate) {
        theForm="searchResultsView:SearchResultsForm:table:";
        checkBox=":searchResultsSelectedCheckbox";
        pinnedBox=":searchResultsPinnedCheckbox";
        for(var i=0; i < source.length; i++){
            len =(source[i].id).length;
            source.submit();
            if(((source[i].id).substr(len-8,len))=="Checkbox") {
                source[i].checked = false;
            }
        }
    }
}

function lastCall(){
    var foundDetailsHere = false;

    for (var i=0; i < document.anchors.length; i++) {
        if ((document.anchors[i].name) == "here") {
            location.href = "#here";
            return false;
        }
        if ((document.anchors[i].name) == "detailsHere") {
            foundDetailsHere = true;
        }
    }

    if (foundDetailsHere) {
        location.href = "#detailsHere";
        return false;
    }

}

function checkboxSelected(checkForm)
{
    var checkboxCount = 0;

    for (var counter = 0; counter < checkForm.length && checkboxCount == 0; counter++)
    {
        var len =0;
        if (checkForm[counter].id !=null && (checkForm[counter].id).length != null) {
            len =(checkForm[counter].id).length;            
            if (((checkForm[counter].id).substr(len-16,len))=="SelectedCheckbox") {
                if (checkForm[counter].checked) {
                    checkboxCount = checkboxCount + 1;
                }
            }
        }
    }

    if (checkboxCount == 0) {
        alert(" Select Checkbox before carrying out an operation.");
        return (false);
    }
}