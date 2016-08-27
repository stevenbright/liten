
function rand(from, to) {
    return Math.floor(Math.random() * (to - from)) + from;
}

function appendRandName(result, parts, minPartsNum, maxPartsNum) {
    minPartsNum = minPartsNum || 1;
    maxPartsNum = maxPartsNum || 1;
    var partCount = rand(minPartsNum, maxPartsNum + 1);
    var i;
    for (i = 0; i < partCount; ++i) {
        var part = parts[rand(0, parts.length)];
        if (part.length === 0) { continue; }
        result.push(part);
    }
}

function randName(parts, minPartsNum, maxPartsNum) {
    var result = [];
    appendRandName(result, parts, minPartsNum, maxPartsNum);
    return result.join(" ");
}

//
// Exports
//

exports.rand = rand;
exports.appendRandName = appendRandName;
exports.randName = randName;
