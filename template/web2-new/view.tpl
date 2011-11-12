<! if:${getvars:callback} >
${getvars:callback}({
<! else >
callback({
<! endif >
<! if:$error >
    "error":"$error",
<! elseif:$local >
    "local":true,
    <! if:$localLoading >
        <! if:$localLoadingErrorPlayer >
        "error":"Could not load local file ($localLoadPath) using player ($player).",
        "loading":false,
        <! elseif:$localLoadingError >
        "error":"Could not determine local file location.
        <a href=\"${episode:link}\" id=\"view-link\">STREAM ${episode:title}</a>",
        "loading":false,
        <! else >
        "title":"${episode:title}",
        "loading":true,
        <! endif >
    <! else >
    "title":"${episode:title}",
    "loading":false,
    <! endif >
<! elseif:${episode:link} >
    "local":false,
    "link":"${episode:link}",
    "title":"${episode:title}",
    <! if:$reset >
    "reset":true,
    <! else >
    "reset":false,
    <! endif >
<! endif >
<! if:${series:nextLink} >
"nextLink":"${series:nextLink}",
"nextTitle":"${series:nextTitle}",
<! endif >
<! if:${series:prevLink} >
"prevLink":"${series:prevLink}",
"prevTitle":"${series:prevTitle}",
<! endif >
"series":"${series:id}"
});
