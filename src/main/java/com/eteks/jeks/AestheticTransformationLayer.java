package com.eteks.jeks;

import javax.swing.table.TableModel;

public interface AestheticTransformationLayer
{
    String toImplementationSyntax(TableModel model, String definition);
    
    String toAestheticSyntax(String definition);
}
