import {MatDialogRef} from "@angular/material/dialog";
import {Component} from "@angular/core";

@Component({
  selector: 'app-input-dialog',
  templateUrl: './simple-input-dialog.component.html'
})
export class SimpleInputDialogComponent {

  public inputValue: string = '';

  constructor(public dialogRef: MatDialogRef<SimpleInputDialogComponent>) { }

  closeDialog(): void {
    this.dialogRef.close(this.inputValue);
  }
}