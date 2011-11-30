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
    link.addClass('selected');

    var letter = link.get('text');
    var targetClass = 'group-'+letter;
    $('all').getElements('.series').each(function(el) {
        if (el.hasClass(targetClass))
            el.removeClass('hidden');
        else
            el.addClass('hidden');
    });

};

var show_all = function(e) {
    if (e != null) new Event(e).stop();

    $$('.jump-link.selected').each(function(el) {
        el.removeClass('selected');
    });
    $$('.series.hidden').each(function(el) {
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
