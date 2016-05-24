<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-component name="head">
    <script type="text/javascript">

        function showOnLoadSpecific(currentDisplayType) {
            
            if (currentDisplayType == 'VOCABULARY') {
                document.getElementById('vocabulary').style.display = "inline";
            } else if (currentDisplayType == 'SELECT') {
                document.getElementById('select').style.display = "inline";
            }
        }

        function showOnChange(elem, currentDisplayType) {

            document.getElementById('vocabulary').style.display = 'none';
            document.getElementById('select').style.display = 'none';

            if (elem.value == currentDisplayType) {
                showOnLoadSpecific(currentDisplayType);
            }
        }
        
        function validateEmptyVocab () {
            var selectedId = document.getElementById('radio');
            if (!selectedId || 0 === selectedId.length) {
                return confirm("No vocabulary selected! This action will remove any existing vocabulary reference to the attribute. Are you sure you want to continue?");
            }
            else return true;
        }

    </script>
</stripes:layout-component>
