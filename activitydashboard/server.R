source("global.R")

require(shiny)
require(shinydashboard)
require(curl)
require(DT)
require(highcharter)
require(shinyjs)

server <- function(input, output, session) {
  
  print("Call Module")
  callModule(overview, "overview")
  
}