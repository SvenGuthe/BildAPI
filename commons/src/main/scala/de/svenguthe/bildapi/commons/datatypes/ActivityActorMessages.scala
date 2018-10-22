package de.svenguthe.bildapi.commons.datatypes

object ActivityActorMessages extends Enumeration {
  type ActivityActorMessages = Value

  val DELETED = Value
  val INSERTED = Value

  val CLOSED = Value

  val PARSED = Value

  val FORWARDED = Value

  val HTTPREQUEST = Value

  val WRONGFORMAT = Value
  val WRONGIDENTIFIER = Value

  val LOG = Value
}
