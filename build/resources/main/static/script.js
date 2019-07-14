const serverUrl = "http://localhost:8080/app";
var loggedUser;
var stompClient = null;
var subscription = null;

var descriere = null;
var imagineSelectata = new Object();

var numeImagini = [];
var descriereImagini = [];

var numeImaginiMatch = [];
var descriereImaginiMatch = [];

var startTime = 0;
var seconds = null;
var interval = null;

var idJoc = null;
var userName = null;

function callPost(url, data, async, callbackFn) {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState === 4 && this.status === 200) {
            callbackFn(this);
        }
    };
    xhttp.open("POST", url, async);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.send(JSON.stringify(data));
}

function callGet(url, async, callbackFn) {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState === 4 && this.status === 200) {
            callbackFn(this);
        }
    };
    xhttp.open("GET", url, async);
    xhttp.send();
}

function connect(idJoc) {
    var socket = new SockJS('http://localhost:8080/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        subscription = stompClient.subscribe(`/update/${idJoc}`, function (payload) {
            const json = eval("(" + payload.body + ")");
            console.log(json);
            update(json);
        });
    });
}

function update(payload) {
    const event = payload.event;
    const object = payload.object;
    switch (event) {
        case "hall":
            $('#sec').text(`Hall Of Fame! Ai rezolvat jocul in ${object / 1000} secunde`);
            openHallOfFame(true);
            return;
        case "final":
            $('#sec').text(`Felicitari! Ai rezolvat jocul in ${object / 1000} secunde`);
            openHallOfFame(false);
            return;
    }
}

function removeMatch(imagine) {
    for(let i = 0; i < numeImagini.length; i++) {
        if(numeImagini[i] === imagine.nume) {
            numeImagini.splice(i, 1);
            break;
        }
    }
    for(let i = 0; i < descriereImagini.length; i++) {
        if(descriereImagini[i] === imagine.descriere) {
            descriereImagini.splice(i, 1);
            break;
        }
    }
    console.log(numeImagini, descriereImagini);
}

function checkMatch(xhttp) {
    const check = eval(xhttp.responseText);
    console.log(check);
    if(check) {
        console.log('selectata', imagineSelectata);
        numeImaginiMatch.push(imagineSelectata.nume);
        descriereImaginiMatch.push(imagineSelectata.descriere);
        removeMatch(imagineSelectata);
        initTable('match', numeImaginiMatch, descriereImaginiMatch);
    }
    descriere = null;
    initTable('init', numeImagini, descriereImagini);
}


function loadTable(xhttp) {
    const list = JSON.parse(xhttp.responseText);
    console.log(list);
    for(let i = 0; i < list.length; i++) {
        numeImagini.push(list[i].nume);
        descriereImagini.push(list[i].descriere);
    }
    initTable('init', numeImagini, descriereImagini);
}

function initTable(tableId, numeImagini, descriereImagini) {
    $('#' + tableId).empty();
    for(let i = 0; i < numeImagini.length; i++) {
        const tr = document.createElement('tr');

        const nume = document.createElement('td');
        const img = document.createElement('img');
        $(img).attr('src', numeImagini[i]).width(150).height(150);
        $(nume).append(img);
        $(tr).append(nume);
        $(nume).click(function () {
           if(descriere) {
               $(this).css('border', 'solid red');
               const numeImg = numeImagini[i];
               imagineSelectata.nume = numeImg;
               console.log(numeImg, descriere);
               setTimeout(function () {
                   callGet(`${serverUrl}/check/${idJoc}/${numeImg}/${descriere}`, false, checkMatch);
               }, 3000);
           }
        });

        const descr = document.createElement('td');
        $(descr).text(descriereImagini[i]);
        $(tr).append(descr);
        $(descr).click(function () {
            if(!descriere) {
                $(this).css('border', 'solid red');
                descriere = $(this).text();
                imagineSelectata.descriere = descriere;
            }
        });

        $('#' + tableId).append(tr);
    }
}

function startJoc(xhttp) {
    idJoc = xhttp.responseText;

    console.log(idJoc);
    connect(idJoc);
}

$(document).ready(function () {
    const username = sessionStorage.getItem('username');
    if(username) {
        userName = username;
        $('#username').val(username);
        $('#form').show();
    }

    startTime = 1;
    interval = setInterval(function () {
        $('#timp').text(startTime);
        startTime++;
    }, 1000);

    callGet(`${serverUrl}/start`, false, startJoc);

    callGet(`${serverUrl}/imagini`, false, loadTable);

});


function openHallOfFame(boolean) {
    $('#joc').show();
    clearInterval(interval);
    var modal = document.getElementById("myModal");
    var span = document.getElementById("close");

    if(boolean) {
        const username = sessionStorage.getItem('username');
        if(username) {
            callGet(`${serverUrl}/hall/${idJoc}/${username}`, false, function (xhttp) {
                const dto = JSON.parse(xhttp.responseText);
                for(let i = 0; i < dto.users.length; i++) {
                    const li = document.createElement('li');
                    $(li).text('' + dto.users[i].username + ' :  ' + dto.users[i].secunde);
                    $('#list').append(li);
                }
            });
        } else {
            $('#hall').show();
        }
    }

    $(span).click(function () {
        $(modal).css("display", "none");
        $('#hall').hide();
    });

    $(modal).css("display", "block");
}

function adaugatHallOfFame(xhttp) {
    const dto = JSON.parse(xhttp.responseText);
    console.log('a fost adaugat ', dto);
    var modal = document.getElementById("myModal");
    // $(modal).css("display", "none");
    // $('#hall').hide();
    sessionStorage.setItem('username', dto.user.username);
    userName = dto.user.username;
    $('#username').val(userName);
    $('#form').show();

    for(let i = 0; i < dto.users.length; i++) {
        const li = document.createElement('li');
        $(li).text('' + dto.users[i].username + ' :  ' + dto.users[i].secunde);
        $('#list').append(li);
    }
}

function adaugaNumeHallOfFame() {
    const name = $('#nume').val();
    callGet(`${serverUrl}/hall/${idJoc}/${name}`, false, adaugatHallOfFame);
}

function handleJocNou() {
    location.reload();
}