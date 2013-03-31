function select_disha(dish) {
    url = "home?call=pickImages&dish=" + dish;
    $.getJSON(url, function(json) {
        for (i = 0; i < json['imgs'].length; i ++) {
            img = json['imgs'][i]['img'];
            $("#imgs").append("<div><img src=" + img + " /><span>" + img + "</span></div>");
        } 
    });
}

function got_images(images) {
    alert(images);
}
