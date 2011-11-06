<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title>SeriesTracker</title>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" >
<link rel="stylesheet" href="files/css/web2-new.css" type="text/css">
<link rel="icon" type="image/icon" href="files/favicon.ico">
<!-- script type="text/javascript" src="files/js/minmax-1.0.js"></script -->
<script type="text/javascript" src="files/js/mootools-1.3.1-core.js"></script>
<script type="text/javascript" src="files/js/utils.bridge.js"></script>
<script type="text/javascript" src="files/js/letters.js"></script>
<script type="text/javascript" src="files/js/web2.js"></script>
</head>
<body>

<div id="header">
<div>
<h3>Welcome to SeriesTracker web2.0 version!</h3>
<b>Logged in as <a href="@user">${user:name}</a></b><br>
<a href="$config_link" id="options-link"><i>Click here for options and settings</i></a>
</div>
</div>

<div id="content">
<div id="recent">
<a id="recent-left" class="nav-left" href="#"></a>
<div id="recent-container-fix">
<div class="nav-container" id="recent-container">
<! class:recentseries >
<! if:$isManaged >
    <div class="series recent
<! if:$isDone >
series-done
<! endif >
<! if:$hasCover >
has-cover
<! endif >
">
<! else >
<div class="series recent series-missing">
<! endif >
<! if:$nextLink >
    <! if:$prevLink >
<a href="$link" id="series-$id" 
    next="${clean>nextTitle}" nextLink="$nextLink" 
    prev="${clean>prevTitle}" prevLink="$prevLink"
    name="$name"
    class="series-link">
    <! else >
<a href="$link" id="series-$id" 
    next="${clean>nextTitle}" nextLink="$nextLink" 
    name="$name"
    class="series-link">
    <! endif >
<! elseif:$prevLink >
<a href="$link" id="series-$id" 
    prev="${clean>prevTitle}" prevLink="$prevLink"
    name="$name"
    class="series-link">
<! elseif:$isManaged >
<a href="$link" name="$name" id="series-$id" class="series-link">
<! else >
<span class="series-link">$name</span>
<! endif >
<! if:$isManaged >
    <! if:$hasCover >
    <img src="covers/$id" class="series-cover">
    <! else >
    $name
    <! endif >
<! endif >
</a>
</div>
<! endclass >
</div>
</div>
<a id="recent-right" class="nav-right" href="#"></a>
</div>

<div id="letters-holder">
<div id="letters">
<a href="#" class="jump-link selected" id="letter-all">All</a>
Jump: 
<! class:series >
<! if:$letter >
<a href="#$letter" class="jump-link" id="letter-$letter">$letter</a>
<! endif >
<! endclass >
</div>
</div>

<div id="all">
<div id="dummy" class="hidden">
<! class:series >
<! if:$letter >
</div>
<div id="series-$letter" class="series-group">
<! endif >
<! if:$isManaged >
    <! if:$isDone >
    <div class="series series-done">
    <! elseif:$isRecent >
    <div class="series recent">
    <! else >
    <div class="series">
    <! endif >
<! if:$nextLink >
<a href="$link" id="series-$id" 
    next="${clean>nextTitle}" nextLink="$nextLink" 
    prev="${clean>prevTitle}" prevLink="$prevLink"
    class="series-link">$name</a>
<! else >
<a href="$link" id="series-$id" class="series-link">$name</a>
<! endif >

<! else >
<div class="series series-missing">
<span class="series-link">$name</a>
<! endif >
</div>
<! endclass >
</div>
</div>


</div>

<div id="info-holder">
<div id="info" class="hidden">
    <div id="info-name">SeriesName</div>
    <div id="info-next-label">Next: ?</div>
    <div id="info-options">
        <a href="#" id="info-next" class="info-option">Watch Next</a>
        <a href="#" id="info-prev" class="info-option">Watch Previous</a>
        <!-- a href="#" id="info-more" class="info-option">More Episodes</a-->
    </div>

    <div id="more-container">
    <div id="more-list">
    </div>
    </div>
</div>

<script type="text/javascript">
// init these here, because the ps3 browser is retarded
// and doesn't always fire domready events properly
init_letters();
init_web2();
</script>

</body>
</html>
