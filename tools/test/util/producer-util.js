
function sqlStringify(str) {
    if (str == null) {
        return 'null';
    }
    if (str.indexOf("'") < 0) {
        return "'" + str + "'";
    }
    return "'" + str.split("'").join("''") + "'";
}


function nameValuePairs(initFn) {
    var values = {};
    var ins = function (id, name) {
        if (name in values) {
            throw new Error("A value with name=" + name + " has been inserted, existing id=" + values[name]);
        }
        values[name] = id;
    }
    initFn(ins);
    return values;
}

//
// Exports
//

exports.sqlStringify = sqlStringify;
exports.nameValuePairs = nameValuePairs;
