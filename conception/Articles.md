# Structure of Bild Articles / Links

## Start Page
Articles are in div-containers with the following hierarchy.

```
<body> 
-> <div id="outerWrapper"> 
-> <div id="innerWrapper"> 
-> <div class="faux"> 
-> <div class="content">
-> ...
-> <a href="/politik/..."> or <a href="https://www.bild.de/politik/...">
```

## Article Page
The content of the article pages are in the following hierarchy of div-containers

```
<body> 
-> <div id="outerWrapper"> 
-> <div id="innerWrapper"> 
-> <main role="main"> 
-> <div class="faux"> 
-> <div class="content">
-> <article>
```

### Kicker
The subtitle above the main headline. 

```
-> <article>
-> <header>
-> <h1 id="cover">
-> <span class="kicker">
```

### Headline

```
-> <article>
-> <header>
-> <h1 id="cover">
-> <span class="headline">
```

### Publish Date

```
-> <article>
-> <div class="article-meta">
-> <div class="authors">
-> <time class="authors__pubdate" datetime="[TIME]">
```

### Text

```
-> <article>
-> <div class="txt">
-> <p> [TEXT] </p>
```