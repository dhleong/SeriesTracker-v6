<! if:${getvars:callback} >
${getvars:callback}
<! else >
callback
<! endif >
([{}
<! if:$error >
<! else >
<! class:episode >
,{
<! if:$id==${series:lastId} >
"lastViewed":true,
<! endif >
"id":"$id",
"saveLink":"$saveLink",
"title":"${clean>title}",
"nosaveLink":"$noSaveLink",
"html":"<a href=\"$saveLink\" id=\"ep-$id\" class=\"more-ep-link\">$title</a>"
}
<! endclass >
<! endif >
]
);
