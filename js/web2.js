/*
 * Scrolling thingy-related stuff
 */
var SCROLL_SPEED = 1;
var SCROLL_DURATION = 375;
var __scroll_intervals = {}

function scroll(box, dir) {
    __scroll_intervals[box.get('id')] = __scroll.periodical(SCROLL_DURATION, null, [box,dir]);
}
function __scroll(box, dir) {
    var ch = box.getFirst();
    var offset = dir * ch.getSize().x;
    var left = box.getStyle('left').toInt();
    var dest = left + offset;
    if (left + offset > 0)
        dest = 0;

    // this doesn't work on PS3... but oh well
    var width = box.getSize().x;
    var rightArrow = $('recent-right').getPosition().x; // HACK
    if (left + offset + width < rightArrow - 200)
        dest = rightArrow - width - 200;

    box.set('tween', {duration: SCROLL_DURATION});
    box.tween('left', dest);
}

function scroll_stop(box) {
    clearInterval(__scroll_intervals[box.get('id')]);
}

/*
 * Browsing
 */
var browse_series = function(e) {
    new Event(e).stop();

    // get id and other infos
    var id = this.get('id');
    id = id.substr(id.indexOf('-')+1);
    $('info').removeClass('hidden')
        .set('info-id', id)
        .set('info-nextLink', this.get('nextLink'))
        .set('info-prevLink', this.get('prevLink'));
    $('info-name').set('text', this.get('name'));

    var parent = $('series-'+id).getParent();
    if (parent.hasClass('series-missing')) {
        $('info-next-label').removeClass('hidden')
            .set('text','No episodes found');
        $('info-next').addClass('hidden');
        $('info-prev').addClass('hidden');
        //$('info-more').addClass('hidden');
    } else {
        $('info-next').removeClass('hidden');
        //$('info-more').removeClass('hidden');

        // update infos
        var next = this.get('next'); 
        if (next != null) {
            $('info-next')
                .removeClass('hidden')
                .focus();
            $('info-next-label').set('text', 'Next: ' + next);
        } else {
            $('info-next').addClass('hidden');
            $('info-next-label').set('text', '');
        }
    }

    // clear the more list and reload
    $('more-list').empty()
    load_more.delay(50);
}

/*
 * Viewing
 */

var EP_REGEX = new RegExp('ep=([0-9]+)');

var ajMore = new Bridge({
    url:'/browse',
    data: {template: 'web2'},
    onComplete: function(data) {
        if (data.length < 1) {
            alert('Error: No episodes');
            return;
        }

        // fill the more list
        $('more-list').set('html', '');
        Object.each(data, function(item) {
            // because the ps3 doesn't like to create elements
            this.innerHTML += item.html;
        }, $('more-list'));

        // done separately because the ps3 is retarded
        Object.each(data, function(item) {
            //bind_view($('ep-' + item.id), item.id);
            if (item.lastViewed) {
                $('ep-' + item.id).addClass('last-viewed');
            }
        });

        /*
        // also separate because the ps3 is retarded
        if (navigator.appName == 'PLAYSTATION 3') {
            $('more-list').getElement('.last-viewed')
                .addClass('mored');
        }
        */

        //Object.each(data, function(item) {
        for (i=0; i<data.length; i++) {
            var item = data[i];
            bind_view($('ep-' + item.id), item.id);
        }

    }
});


var ajView = new Bridge({
    url:'/view',
    data: {template:'web2'},
    onComplete: function(data) {
        // update infos
        if (data.error)
            alert(data.error);

        if (data.nextLink) {
            $('info').set('info-nextLink', data.nextLink);
            $('info-next-label').set('text', 'Next: ' + data.nextTitle);
            if ($('info-next').hasClass('hidden')) {
                $('info-next').removeClass('hidden');
                $('info-next-label').removeClass('hidden');
            }

            $('series-'+data.series).set('next', data.nextTitle);
            $('series-'+data.series).set('nextLink', data.nextLink);
        } else {
            $('info-next').addClass('hidden');
            $('info-next-label').addClass('hidden');

            $('series-'+data.series).erase('next');
            $('series-'+data.series).erase('nextLink');
        }

        if (data.prevLink) {
            $('series-'+data.series).set('prev', data.prevTitle);
            $('series-'+data.series).set('prevLink', data.prevLink);
        } else {
            $('series-'+data.series).erase('prev');
            $('series-'+data.series).erase('prevLink');
        }

        /*
        $('modal-in').set('html', "<div>Loading " + data.title + "</div>" +
            '<div><a href="#" id="on-done">Click here when done</a></div>');
        $('modal').removeClass('hidden');
        $('on-done').addEvent('click', on_finish_viewing);
        $('modal-in').addEvents({'focus':on_finish_viewing, 'mouseenter':on_finish_viewing});
        */

        // show the movie
        if (!data.local) 
            load_video.delay(50, this, data.link);
    }
});

/** set a link to view an episode onclick */
var bind_view = function(el, ep) {
    el.addEvent('click', (function(e) {
        new Event(e).stop();
        var last = $('more-list').getElement('.last-viewed');
        if (last != null)
            last.removeClass('last-viewed');
        $('ep-' + this).addClass('last-viewed');
        load_episode(this, 1);
    }).bind(ep));
};


var load_episode = function(ep, save) {
    var args = {
        id:$('info').get('info-id'),
        ep:ep,
        save:save
    };
    if (window.location.href.indexOf('localhost') > -1)
        args['local'] = true; // skip the loading phase

    // set the 'last viewed' class
    $$('.last-viewed').each(function(el) {
        el.removeClass('last-viewed');
    });
    var div = $('ep-' + ep);
    if (div != null)
        div.addClass('last-viewed');

    ajView.send(args);
};

/** bindable version to pass through to load_episode */
var load_ep = function(e) {
    new Event(e).stop();

    // 'this' is either 'next' or 'prev'
    var link = $('info').get('info-' + this + 'Link');
    var m = EP_REGEX.exec(link);
    if (m != null && m.length > 1) {
        var ep = m[1];
        load_episode(ep, 1);
    }
}

var load_more = function() {
    //new Event(e).stop();

    var serid = $('info').get('info-id');
    ajMore.send({id:serid});
}

var load_video = function(link) {
    if (navigator.appName == 'PLAYSTATION 3') {
        // ps3 browser
        window.location = link;
    } else {
        // normal browser... open in new window?
        var win = window.open(link);//, '_blank', 'fullscreen=yes');
        /*
        win.addEvent('unload', function() {
            //alert('closed');
        });
        */
    }
};

/*
 * Initializer
 */
function init_web2() {
    var recent_box = $('recent-container');
    $('recent-left').addEvent('mouseover',  scroll.pass([recent_box, SCROLL_SPEED]));
    $('recent-left').addEvent('mouseout', scroll_stop.pass(recent_box));
    $('recent-right').addEvent('mouseover', scroll.pass([recent_box, -SCROLL_SPEED]));
    $('recent-right').addEvent('mouseout', scroll_stop.pass(recent_box));

    $$('.series-link').each(function(el) {
        el.addEvent('click', browse_series);
    });

    $('info-next').addEvent('click', load_ep.bind('next'));
    $('info-prev').addEvent('click', load_ep.bind('prev'));
    //$('br-more').addEvent('click', show_more);
}
