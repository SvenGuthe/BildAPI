source("global.R")

require(shiny)
require(shinydashboard)
require(curl)
require(DT)
require(highcharter)
require(shinyjs)

ui <-dashboardPage(
  
  dashboardHeader( title = "Monitoring" ),
  
  dashboardSidebar(
    
    sidebarMenu(
      
      menuItem("Overview", tabName = "overview", icon = icon("dashboard"), selected = TRUE)

    )
    
  ),
  
  dashboardBody(
    
    tabItems(
      
      tabItem( tabName = "overview",
               
               overview("overview")
               
      )
      
    )
    
  )
  
) 