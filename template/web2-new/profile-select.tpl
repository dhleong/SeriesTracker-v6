<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title>ST - $title Selection</title>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" >
<! if:$refresh >
<meta http-equiv="refresh" content="$refresh">
<! endif >
<link rel="stylesheet" href="files/css/default.css" type="text/css">
</head>
<body>
<h3>SeriesTracker - $title Selection</h3>

<div>
<! if:$body >
$body
<! endif >
<ul>
<! class:select_item >
<li><a href="$link">$name</a></li>
<! endclass >
</ul>
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
