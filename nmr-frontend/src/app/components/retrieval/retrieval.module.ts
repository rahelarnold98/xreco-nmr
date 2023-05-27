import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {CommonModule, NgOptimizedImage} from "@angular/common";
import {SearchComponent} from "./search/search.component";
import {MatButtonModule} from "@angular/material/button";
import {FormsModule} from "@angular/forms";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatMenuModule} from "@angular/material/menu";
import {MatListModule} from "@angular/material/list";
import {GeneralModule} from "../general/general.module";
import {MatDialog, MatDialogModule} from "@angular/material/dialog";
import {DetailsComponent} from "./details/details.component";
import {MatCardModule} from "@angular/material/card";
import {MatBadgeModule} from "@angular/material/badge";

@NgModule({
    declarations: [SearchComponent, DetailsComponent],
  imports: [
    GeneralModule,
    BrowserModule,
    CommonModule,
    NgOptimizedImage,
    MatButtonModule,
    FormsModule,
    MatDialogModule,
    MatIconModule,
    MatTooltipModule,
    MatMenuModule,
    MatListModule,
    MatCardModule,
    MatBadgeModule
  ],
    providers: [],
    bootstrap: []
})
export class RetrievalModule { }