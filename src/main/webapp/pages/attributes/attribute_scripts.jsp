<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-component name="head">
    <script type="text/javascript">

        function showOnChange(elem, currentDisplayType) {

            document.getElementById('vocabulary').style.display = 'none';
            document.getElementById('select').style.display = 'none';

            if (elem.value == currentDisplayType) {
                if (currentDisplayType == 'VOCABULARY') {
                    document.getElementById('vocabulary').style.display = "inline";
                } else if (currentDisplayType == 'SELECT') {
                    document.getElementById('select').style.display = "inline";
                }
            }
        }
        
        function validateEmptyVocab () {
            var selectedId = document.getElementById('radio');
            if (!selectedId || 0 === selectedId.length) {
                alert("Please select a vocabulary!");
                return false;
            }
            else return true;
        }
        
        function validateMandatoryEditorFields () {
            var list = document.getElementsByClassName("mandatory_field");
            var invalidList = "";
            for (var i = 0; i < list.length; i++) {
                var entry = list[i];
                if (!entry.value || 0 === entry.value.length) {
                    invalidList = invalidList + entry.id + "\n";
                    console.log("Entry invalid: "+ entry.id);
                }   
            }
            if (invalidList.length !== 0){
                alert("Please insert values for fields: \n" + invalidList);
                return false;
            }
            return true;
        }

    </script>
</stripes:layout-component>
