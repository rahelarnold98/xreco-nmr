import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {CommonModule, NgOptimizedImage} from "@angular/common";
import {MatButtonModule} from "@angular/material/button";
import {FormsModule} from "@angular/forms";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatMenuModule} from "@angular/material/menu";
import {MatListModule} from "@angular/material/list";
import {SimpleInputDialogComponent} from "./simple-input-dialog.component";
import {MatInputModule} from "@angular/material/input";

@NgModule({
  declarations: [SimpleInputDialogComponent],
  exports: [SimpleInputDialogComponent],
  imports: [
    BrowserModule,
    CommonModule,
    NgOptimizedImage,
    MatButtonModule,
    FormsModule,
    MatIconModule,
    MatTooltipModule,
    MatMenuModule,
    MatListModule,
    MatInputModule
  ],
  providers: []
})
export class GeneralModule { }