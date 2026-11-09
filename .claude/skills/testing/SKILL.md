---
name: testing
description: Rules for writing tests - JUnit, TestFX for headless Java FX UI tests, package mirroring, coverage and the developer test vs. integration test ("IT" suffix) vs. regression test ("RT" suffix) split. Load before creating or changing any test class.
---

# Testing

* JUnit test system MUST be used
* For Java FX UI tests use TestFX framework
  * Must run in the background without an open UI
* Every use case MUST be tested
* Code coverage should reach 100%
* The package structure of the production code is to be mirrored
* EVERY test method is to be documented with a detailed KDoc describing the use case
* ALL test data MUST be written in ENGLISH
    * This covers sample texts, names, JSON fixtures and expected values
    * Exception: a test that verifies a specific language or locale on purpose
* A UI component bound to an FX property model MUST prove the binding in BOTH directions
    * The proof MUST be made on the real controls the user works with - the `TextField`, the
      `TextArea`, the button - NOT on the properties of the view model alone
    * Direction "UI -> model": what is written into EVERY control MUST stand in the POJO of
      `lib/model` afterwards, read from the POJO itself, not from a property
    * Direction "model -> UI": a value written through the FX property model MUST stand in the
      control afterwards
    * A value written on the POJO past the property model, followed by `refresh()`, MUST reach the
      control as well
    * Adding, removing and exchanging entries of a list MUST be proven in both directions, including
      that the remaining controls still write to their own entry
    * Exchanging the bound model object MUST be proven: the controls show the new object and write
      into it only, the object left behind stays untouched
* Tests are to be split into three categories
    * **Developer tests** - Simple unit tests covering individual pieces of functionality
      * All Test Classes without suffix of "IT" or "RT"
    * **Integration tests** - Tests covering complete features or aiming at performance
      * Identified by Class Name ending with "IT"
      * ONLY allowed in application modules under `app`
      * FORBIDDEN in library modules under `lib`
    * **Regression tests** - Tests feeding input through several classes of one or more libraries
      and comparing the produced result against a fixed reference (e.g. a golden file)
      * Identified by Class Name ending with "RT"
      * ONLY allowed in library modules under `lib`
      * FORBIDDEN in application modules under `app` - use "IT" there instead
      * A test spanning several library classes without comparing against a fixed reference stays
        a developer test without the "RT" suffix
