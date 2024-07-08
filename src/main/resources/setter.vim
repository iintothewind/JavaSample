#if ($field.modifiers.contains("private"))
public void set${field.capName}(${field.type} ${field.name}) {
    this.${field.name} = ${field.name};
}
#end
