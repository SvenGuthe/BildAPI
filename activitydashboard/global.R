#Script to install all Packages automatically

list.of.packages <- c("curl", "shiny", "shinydashboard", "DT", "highcharter", "shinyjs")
new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages)

options(encoding = "UTF-8")

source("./modules/overview/overview.R")
source("./modules/overview/overviewUI.R")