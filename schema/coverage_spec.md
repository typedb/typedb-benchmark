# Graql World Simulation Coverage

In this document we use markdown checkmarks to indicate patterns that have been included in the schema, brackets to enclose an example of where in the schema, and `Implemented` if the simulation has a working agent that makes use of this/such a pattern.

## Design patterns to cover

Here we have some classic design patterns that recur commonly in Graql schemas for all domains.

- [x] `Implemented` Group membership (`membership`, `employment`)
- [x] `Implemented` Transitive hierarchy (`location-hierarchy-transitivity`)
- [x] `Implemented` Transitive group membership (`born-in-transitivity`)
- [x] `Implemented` Data-sourcing/prediction/location nested relations (`marriage plays locates_located`)

## Patterns

Following are schema/data patterns that are cases that we would like to ensure work correctly and quickly. This may skip over obvious cases which have been assumed and already added to the World Simulation schema, in effect making this more of a running TODO.

### Abstract

- Abstract type hierarchy
  - [x] `Implemented` Entities (`location`)
  - [ ] Relations (candidate: `transaction`, `b2c-transaction`, `b2b-transaction`)
  - [x] `Implemented` Attributes (`date-of-event`)
- Non-abstract type hierarchy
	- [x] `Implemented` Entities (`organisation`)
  - [x] `Only employment is implemented` Relations (`membership`)
  - [x] `Implemented` Attributes (`name`)
- [ ] `Necessary?` Multiple abstract types in an inheritance
### Keys 
- [x] `Implemented` Different types using the same attribute as their key (`location-name`, although `continent`, `country`, `city` are all within the `location` hierarchy)
- [ ] Different types owning the same attribute, one as its key, the other not keyed
- [x] `Only product-barcode implemented` Types using attributes as keys where the attribute types form a hierarchy (`identifier-double`)
- Keying with each datatype:
	- [x] `Implemented` long (`marriage-id`, `company-number`)
	- [x] `Implemented` double (`product-barcode`)
	- [x] `Implemented` string (`email`, `location-name`)
	- [ ] boolean
	- [ ] date
- [ ] Multiple keys for one thing type
- [ ] Multiple keys for one thing type, with different datatypes
- [x] `Disallowed` ~~An attribute owned by the parent but used as key by the child~~
- [ ] `Possible? Necessary?` Keying on both of two attributes that are in a hierarchy together
- [ ] `Possible?` Can a subtype of a key be used to key the thing keyed by its super type (at insertion time)?

### Attributes

- [ ] Attribute playing a (non-implicit) role in a relation
- [x] `Easy to implement language on text-content of employment-contract` Attribute of attribute (`language`)
- [ ] `Deemed unnecessary for now` ~~Attribute of attribute of attribute~~
- [x] `Implemented` Attribute hierarchies (`sub date-of-event`)
- All attribute datatypes (not as keys):
	- [x] `Implemented` long (`product-quantity`)
	- [x] `Implemented` double (`annual-wage`)
	- [x] `Implemented` string `forename`
	- [ ] `Easy to implement` boolean
	- [x] `Implemented` date (`start-date`)
- Implicit relations:
  - [ ] adding new roleplayers to them
  - [x] `Easy to implement currency on annual-wage` adding attributes to them
- [x] `Only implemented for initialisation data` Regex for strings (`currency`, `currency-code`)
- [ ] `Implement as an update of people's age based on DoB` Attribute value updates (deletion of implicit relations with via and adding new attribute)
- [ ] Box shape where two things are in a hierarchy each owning an attribute, where the attributes are also in a hierarchy

### Relations

Ternary and N-ary relations, with interesting numbers of roleplayers per role

- [x] `Implemented`One entity playing 3+ roles (`person`, `company`, `location`)
- [ ] One relation playing 2+ roles
- [ ] One relation playing 3+ roles
- [x] `Implemented` One relation with 3+ roles played (`transaction`. `relocation`)
- [ ] Unary relation, where a thing instance plays one role once and there are no other roleplayers
- [x] Symmetric relation (`friendship`, idea: `project-collaboration`)
- [x] `Implemented` Antisymmetric relation (`employment`, `location-hierarchy`, `marriage`, `transaction`...)
- [x] Transitive relation
- Reflexive relation (`data-sourcing` as an example relation, or a court ruling, which could be on another ruling). There are two senses here:
	- [ ] A relation that plays a role in itself
	- [ ] A thing instance that plays two roles in the same relation instance
- [x] `Implemented` The anti-transitive and anti-reflexive are defined easily by the lack of a transitive rule or a reflexive role
- [x] `Implemented` Relation/entity/attribute types playing two roles in a relation, where the instances only play one role each (`person` playing `husband` and `wife` in a marriage)
- [x] `Implemented` Ragged role hierarchy: a relation that inherits from a parent subtypes the parent's roles, but also introduces new roles not yet seen in the hierarchy (`employment_contract` role)
- [x] `Necessary?` Inheriting from a role declared in the current relation (not the parent)
- [ ] `Allowed?` Inherit role from grandparent relation, skipping the parent

## Rule formulations
- [x] `Implemented` Inferred relation (`born-in-transitivity`, `born-in-location-implies-residency `...)
- [ ] Inferred relation where the rule is written for the parent relation type
- [x] `Implemented` Rules for relations that also have materialised instances (`born-in-transitivity`, `location-hierarchy-transitivity`)
- [x] `Implemented` Inferred attribute (`born-in-location-implies-residency-date`, `person-relocating-ends-old-residency`, `person-membership-of-organisation-means-relocation-date`)
- Inferred attribute using value from the `when`
	- [x] `Implemented` as the same type in the `then` (`transaction-currency-is-that-of-the-country`)
	- [x] `Implemented` transposing to a different type in the `then` (`person-relocating-ends-old-residency`, `born-in-location-implies-residency-date`)
- [ ] Inferred complex type
- [ ] `Necessary?` Inferred entity
- [x] `Implemented` Successive/compound/recursive rules (those regarding `born-in` and `residency`)
- [x] `Implemented` `when` bodies with a relation(s) where the relation(s) don't have a variable ascribed to them (`person-membership-of-organisation-means-relocation`)
- [ ] Utilising explanations of rules, also recursively

## World elements to model

- [x] Trade between companies
- [ ] Governments and elections
- [ ] Legal system
- [ ] Taxation and finance
- [ ] Currency
- [ ] Scientific measurements
- [ ] Immigration/emmigration
- [x] Residency
- [ ] Phone numbers for regex and country extensions

## Other concerns

- [ ] Data Deletion
- [ ] New schema additions or undefine statements
- [ ] (Future) see how quickly an indexed schema pattern comes into effect and the speed improvement
- Matching across data and schema in single queries, e.g.:
  - [ ] `match $d type my-attribute; $d datatype string; get;`
- Supernodes
  - [ ] Entities
  - [ ] Attributes
  - [ ] Relations