function change(lang) {
    $.ajax({
        type:'post',
        url:'/changeLang',
        dataType:'json',
        data:{
            lang:lang
        },success:function (data) {
            if(data="success"){
                sessionStorage.removeItem("language");
                sessionStorage.setItem("language",lang);
                location.reload();
            }
        }
    })
}