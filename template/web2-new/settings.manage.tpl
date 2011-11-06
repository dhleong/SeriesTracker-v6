<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title>STv5 - Settings</title>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" >
<link rel="stylesheet" href="files/css/default.css" type="text/css">
<script type="text/javascript" src="files/js/mootools-1.3.1-core.js"></script>
<script type="text/javascript" src="files/js/utils.manage.js"></script>
</head>
<body>
<div>
<h3>SeriesTracker v5 Settings</h3>
<div>
Found $available folders.<br>
Check the folders you wish to track as series. If the series is already tracked, 
it will be highlighted, and its current episode will not be affected.
</div>
<div id="tools">
<div id="select-tools">
<h5>Quick selection</h5>
<input type="button" id="select-all" value="Select all"> | 
<input type="button" id="select-none" value="Select none"> |
<input type="button" id="select-available" value="Select available">
<div class="small-text">
Note that these operate even on hidden series! Unhide everything to make sure!
</div>
</div>

<div id="hide-tools">
<h5>Hide series that are:</h5>
<div class="small-text">
<input type="checkbox" name="hide-unavailable" id="hide-unavailable" CHECKED>
<label for="hide-unavailable">Not found</label>
<input type="checkbox" name="hide-in-profile" id="hide-in-profile">
<label for="hide-in-profile">Already in profile</label>
</div>
</div>
</div>

<form action="$formAction" method="POST" id="theform">
<div>
<ul class="rebuild-list" id="rebuild-list">
<! class:availableSeries >
<! if:$isInProfile >
<! if:$localPath >
<li class="in-profile">
<! else >
<li class="in-profile unavailable">
<! endif >
<input type="checkbox" name="accepted[]" value="$INDEX" id="$INDEX" CHECKED>
<! else >
<! if:$localPath >
<li>
<! else >
<li class="unavailable">
<! endif >
<input type="checkbox" name="accepted[]" value="$INDEX" id="$INDEX">
<! endif >
<label for="$INDEX">$name
<input type="hidden" name="inProfile[]" value="$isInProfile">
<input type="hidden" name="ids[]" value="$id">
<input type="hidden" name="names[]" value="${encode>name}">
<! if:$localPath >
<br><span class="local-path" id="pathFor_$INDEX">$localPath</span>
<! endif >
</label>
</li>
<! endclass >
</ul>
</div>

<div><input type="submit" value="Save changes"></div>
</form>

<div>
<ul class="nav">
<li><a href="$homeLink">Go home</a></li>
</ul>
</div>
</body>
</html>
