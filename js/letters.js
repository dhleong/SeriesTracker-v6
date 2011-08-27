/**
 * Letter navigation for web2 template set
 */

var switch_letter_cb = function(e) {
    new Event(e).stop();

    switch_letter(this);
}

var switch_letter = function(link) {
    $$('.jump-link.selected').each(function(el) {
        el.removeClass('selected');
    });
    $$('.series-group').each(function(el) {
        el.addClass('hidden');
    });

    link.addClass('selected');
    var letter = link.get('text');
    $('series-' + letter).removeClass('hidden');
};

var show_all = function(e) {
    if (e != null) new Event(e).stop();

    $$('.jump-link.selected').each(function(el) {
        el.removeClass('selected');
    });
    $$('.series-group').each(function(el) {
        el.removeClass('hidden');
    });
    $('letter-all').addClass('selected');
}

var init_letters = function() {
    $$('.jump-link').each(function(l) {
        l.addEvent('click', switch_letter_cb);
    });

    $('letter-all').removeEvents()
        .addEvent('click', show_all)

    show_all();
};
