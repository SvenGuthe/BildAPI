# Project Modules

![Overview](graphics/Overview.png)

## urlcrawler
Is searching for new URLs on the Bild Website.

![URL Crawer](graphics/URLCrawler.png)

## crawler
Downloads the articles.

## filter
Checks if the articles from the crawler are bild-articles.

## decoder
Checks the section of the article and decode it in scala-objects.

## storer
Store the objects to cassandra.

## cleaner
Checks the cassandra url list for duplicates and remove them.

![Cleaner](graphics/Cleaner.png)

## analyzer
Analyze the given information about the articles.

## api
An interface for users.

## visualizer
Visualization of analyzed information about Bild articles.