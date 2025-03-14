<!---
.. ===============LICENSE_START=======================================================
.. Acumos CC-BY-4.0
.. ===================================================================================
.. Copyright (C) 2019 Nordix Foundation
.. ===================================================================================
.. This Acumos documentation file is distributed by AT&T and Tech Mahindra
.. under the Creative Commons Attribution 4.0 International License (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..
.. http://creativecommons.org/licenses/by/4.0
..
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================
-->

# LicenseProfileEditor

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 8.1.0.

## Setup

Run `npm install` for installing node modules.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Running iframe-license-editor for local testing

- Open Terminal 1 and run `npm run start:lite-server` - this will start the lite-server on port 3000 and watches the `dist/license-profile-editor` directory for any changes.
- Open Terminal 2 and run `ng build --prod --watch` - this will build and copy output under `dist` folder.
- Once above command is over, Open Terminal 3 and run `npm run copy:iframe` - this will copy the `iframe-license-editor.html` to `dist\license-profile-editor` directory.
  - NOTE: you need to re-run copy task, if any changes to `iframe-license-editor.html` file.
- Open browser and load http://localhost:3000/iframe-license-editor.html document.

## Running license-profile-editor as web component for local testing

- Open Terminal 1 and run `npm run start:lite-server` - this will start the lite-server on port 3000 and watches the `dist/license-profile-editor` directory for any changes.
- Open Terminal 2 and run `npm run build:elements` - this will build and copy output under `dist` folder + copy the web-cmp.html file
- Open browser and load http://localhost:3000/web-cmp.html document.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
