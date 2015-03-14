# Introduction #

The validation is accesible through IValidator interface. As of now, the only supported implementation is a Java wrapper to [VAL validator](http://www.dcs.kcl.ac.uk/staff/andrew/planning/index.php?option=com_content&view=article&id=70&Itemid=77), which is included in binary form for both Windows (compiled with CygWin) and Linux.


# Maven dependency #

There is a separate artifact for VAL validator (> 11 MB!)

```
        <dependency>
            <groupId>cz.cuni.amis.planning4j.external</groupId>
            <artifactId>external-validation</artifactId>
        </dependency>            
```

# Usage #

Either use `ValValidator` class directly, or use `Planning4JUtils.validate()` helper method (supports domain and problem provider translation). Before using the validator, it has to be extracted with `ValValidator.extractAndPrepareValidator()`

More documention will be (hopefully) added in the future, meanwhile see JavaDoc.