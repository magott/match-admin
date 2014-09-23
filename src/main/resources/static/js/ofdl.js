function trioValidation(value, element, param) {
    var selectedIndex = document.getElementById('level').selectedIndex
    return !($('#refTypeDommer').is(':checked') && (selectedIndex  < 5 || selectedIndex == 17));
}