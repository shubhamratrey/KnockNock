package com.sillylife.knocknock.database

import com.google.gson.Gson
import com.sillylife.knocknock.database.entities.ContactsEntity
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.utils.CommonUtil
import java.util.*

object MapDbEntities {

    fun contactToEntity(contacts: Contact): ContactsEntity {
        val entity = ContactsEntity()
        if (CommonUtil.textIsNotEmpty(contacts.name))
            entity.name = contacts.name
        if (CommonUtil.textIsNotEmpty(contacts.firstName))
            entity.firstName = contacts.firstName
        if (CommonUtil.textIsNotEmpty(contacts.middleName))
            entity.middleName = contacts.middleName
        if (CommonUtil.textIsNotEmpty(contacts.lastName))
            entity.lastName = contacts.lastName
        if (CommonUtil.textIsNotEmpty(contacts.username))
            entity.username = contacts.username
        if (CommonUtil.textIsNotEmpty(contacts.phone))
            entity.phone = contacts.phone
        if (CommonUtil.textIsNotEmpty(contacts.image))
            entity.image = contacts.image
        if (CommonUtil.textIsNotEmpty(contacts.lat))
            entity.latitude = contacts.lat
        if (CommonUtil.textIsNotEmpty(contacts.image))
            entity.longitude = contacts.long
        if (contacts.hasInvited != null)
            entity.hasInvited = contacts.hasInvited
        if (contacts.availableOnPlatform != null)
            entity.availableOnPlatform = contacts.availableOnPlatform
        if (contacts.lastConnected != null)
            entity.lastConnected = contacts.lastConnected?.time
        entity.raw = Gson().toJson(contacts)
        return entity
    }

    fun contactToEntity(contactsEntity: ContactsEntity): Contact {
        val contact = Contact()
        contact.name = contactsEntity.name
        contact.phone = contactsEntity.phone
        contact.image = contactsEntity.image
        contact.lat = contactsEntity.latitude
        contact.long = contactsEntity.longitude
        contact.userPtrId = contactsEntity.userPtrId
        contact.hasInvited = contactsEntity.hasInvited
        contact.availableOnPlatform = contactsEntity.availableOnPlatform
        if (contactsEntity.lastConnected == null) {
            contact.lastConnected = null
        } else {
            contact.lastConnected = Date(contactsEntity.lastConnected!!)
        }
        return contact
    }

}
