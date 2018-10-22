package de.svenguthe.bildapi.commons.datatypes

object ActivityActorMessages extends Enumeration {
  type ActivityActorMessages = Value

  val DELETED = Value
  val INSERTED = Value

  val WRONGFORMAT = Value
  val WRONGIDENTIFIER = Value

  val LOG = Value
}
