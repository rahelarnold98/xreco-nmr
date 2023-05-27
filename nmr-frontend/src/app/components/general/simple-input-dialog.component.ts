import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Component, Inject} from "@angular/core";

@Component({
  selector: 'app-input-dialog',
  templateUrl: './simple-input-dialog.component.html'
})
export class SimpleInputDialogComponent {
  /** The value for this {@link SimpleInputDialogComponent}. */
  public value: string = '';

  /** The title for this {@link SimpleInputDialogComponent}. */
  public readonly title: string = '';

  constructor(@Inject(MAT_DIALOG_DATA) data: {title: string | undefined, value: string | undefined}, public dialogRef: MatDialogRef<SimpleInputDialogComponent>) {
    this.title = data?.title ? data.title : 'Input Dialog'
    this.value = data?.value ? data.value : ''
  }

  /**
   * Closes the dialog and returns the value.
   */
  public return(): void {
    this.dialogRef.close(this.value);
  }
}