<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<! if:$title >
<title>$title</title>
<! else >
<title>STv5 - Settings</title>
<! endif >
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" >
<! if:$refresh >
<meta http-equiv="refresh" content="$refresh">
<! endif >
<link rel="stylesheet" href="files/css/default.css" type="text/css">
</head>
<body>
<! if:$title >
<h3>$title</h3>
<! else >
<h3>SeriesTracker v5 Settings</h3>
<! endif >
<div>
$body
</div>

<! if:$homeLink >
<div>
<ul class="nav">
<li><a href="$homeLink">Go home</a></li>
</ul>
</div>
<! endif >

</body>
</html>
