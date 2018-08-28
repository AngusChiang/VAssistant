
importClass(Packages.cn.vove7.vtp.sharedpreference.SpHelper)
function newSpHelper(name){
    log('sp: ', name);
    return new SpHelper(app, name);
}

