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

import { BrowserModule } from '@angular/platform-browser';
import { NgModule, Injector } from '@angular/core';

import { AcumosMaterialDesignFramework } from './AcumosMaterialDesignFramework';
import { LicenseRtuEditorComponent } from './license-rtu-editor/license-rtu-editor.component';
import { createCustomElement } from '@angular/elements';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatFormFieldModule, MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatMenuModule } from '@angular/material/menu';
import { MaterialDesignFrameworkModule, ɵb as Framework } from '@earlyster/angular6-json-schema-form';
import { HttpClientModule } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { Bootstrap4FrameworkModule } from '@earlyster/angular6-json-schema-form';

@NgModule({
  declarations: [
    LicenseRtuEditorComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    MatCardModule, MatToolbarModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatGridListModule,
    MatMenuModule,
    MatIconModule,
    MatRadioModule,
    MatSnackBarModule,
    MaterialDesignFrameworkModule,
    HttpClientModule,
    FormsModule,
    Bootstrap4FrameworkModule
  ],
  providers: [
    {
      provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
      useValue: {
        appearance: 'outline'
      }
    },
    {
      provide: Framework,
      useClass: AcumosMaterialDesignFramework,
      multi: true
    }
  ],
  entryComponents: [LicenseRtuEditorComponent]

})
export class AppModule {

  constructor(private injector: Injector) {}

  ngDoBootstrap() {
    // using createCustomElement from angular package it will convert angular component to stander web component
    const el = createCustomElement(LicenseRtuEditorComponent, {
      injector: this.injector
    });
    // using built in the browser to create your own custome element name
    customElements.define('license-rtu-editor', el);
  }
 }
