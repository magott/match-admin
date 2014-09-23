function trioValidation(value, element, param) {
    var selectedIndex = document.getElementById('level').selectedIndex
    return !($('#refTypeDommer').is(':checked') && (selectedIndex  < 4 || selectedIndex == 17));
}