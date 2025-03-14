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

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LicenseRtuEditorComponent } from './license-rtu-editor.component';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatCardModule, MatToolbarModule, MatFormFieldModule,
         MatInputModule, MatButtonModule, MatRadioModule,
         MatGridListModule, MatIconModule, MatSnackBarModule } from '@angular/material';
import { MaterialDesignFrameworkModule } from '@earlyster/angular6-json-schema-form';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatMenuModule } from '@angular/material/menu';
import { FormsModule } from '@angular/forms';

describe('LicenseRtuEditorComponent', () => {
  let component: LicenseRtuEditorComponent;
  let fixture: ComponentFixture<LicenseRtuEditorComponent>;

  beforeEach(async(() => {

    TestBed.configureTestingModule({
      declarations: [ LicenseRtuEditorComponent ],
      imports: [
        BrowserModule,
        FormsModule,
        MatMenuModule,
        BrowserAnimationsModule,
        MatCardModule, MatToolbarModule, MatFormFieldModule, MatInputModule, MatButtonModule,
        MatRadioModule,
        MatGridListModule,
        MaterialDesignFrameworkModule,
        HttpClientTestingModule,
        MatIconModule,
        MatSnackBarModule,
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LicenseRtuEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
