/*-
 * ===============LICENSE_START================================================
 * Acumos Apache-2.0
 * ============================================================================
 * Copyright (C) 2019 Nordix Foundation.
 * ============================================================================
 * This Acumos software file is distributed by Nordix Foundation
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END==================================================
 */

import { ɵbg as FlexLayoutRootComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbh as FlexLayoutSectionComponent } from '@earlyster/angular6-json-schema-form';
import { ɵb as Framework } from '@earlyster/angular6-json-schema-form';
import { Injectable } from '@angular/core';
import { ɵbi as MaterialAddReferenceComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbk as MaterialButtonComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbl as MaterialButtonGroupComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbm as MaterialCheckboxComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbn as MaterialCheckboxesComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbo as MaterialChipListComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbp as MaterialDatepickerComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbz as MaterialDesignFrameworkComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbq as MaterialFileComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbr as MaterialInputComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbs as MaterialNumberComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbj as MaterialOneOfComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbt as MaterialRadiosComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbu as MaterialSelectComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbv as MaterialSliderComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbw as MaterialStepperComponent } from '@earlyster/angular6-json-schema-form';
import { ɵbx as MaterialTabsComponent } from '@earlyster/angular6-json-schema-form';
import { ɵby as MaterialTextareaComponent } from '@earlyster/angular6-json-schema-form';


// Material Design Framework
// https://github.com/angular/material2

@Injectable()
export class AcumosMaterialDesignFramework extends Framework {
  name = 'acumos-material-design';

  framework = MaterialDesignFrameworkComponent;

  stylesheets = [
  ];

  widgets = {
    root:            FlexLayoutRootComponent,
    section:         FlexLayoutSectionComponent,
    $ref:            MaterialAddReferenceComponent,
    button:          MaterialButtonComponent,
    'button-group':    MaterialButtonGroupComponent,
    checkbox:        MaterialCheckboxComponent,
    checkboxes:      MaterialCheckboxesComponent,
    'chip-list':       MaterialChipListComponent,
    date:            MaterialDatepickerComponent,
    file:            MaterialFileComponent,
    number:          MaterialNumberComponent,
    'one-of':          MaterialOneOfComponent,
    radios:          MaterialRadiosComponent,
    select:          MaterialSelectComponent,
    slider:          MaterialSliderComponent,
    stepper:         MaterialStepperComponent,
    tabs:            MaterialTabsComponent,
    text:            MaterialInputComponent,
    textarea:        MaterialTextareaComponent,
    'alt-date':        'date',
    'any-of':          'one-of',
    card:            'section',
    color:           'text',
    'expansion-panel': 'section',
    hidden:          'none',
    image:           'none',
    integer:         'number',
    radiobuttons:    'button-group',
    range:           'slider',
    submit:          'button',
    tagsinput:       'chip-list',
    wizard:          'stepper',
  };
}
