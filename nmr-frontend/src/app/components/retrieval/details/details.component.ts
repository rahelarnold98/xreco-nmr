import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {MediaResource, ResourceService} from "../../../../../openapi";
import {BehaviorSubject} from "rxjs";

@Component({
  selector: 'nmr-details',
  templateUrl: './details.component.html',
  styleUrls: ['./details.component.scss']
})
export class DetailsComponent {

  /** The ID of the currently active {@link MediaResource}. */
  public readonly mediaResourceId : string

  /** The ID of the currently active {@link MediaResource}. */
  public readonly timestamp : number | undefined

  /** The {@link MediaResource} displayed by this {@link DetailsComponent}. */
  private _mediaResource = new BehaviorSubject<MediaResource | null>(null)
  public mediaResource = this._mediaResource.asObservable()

  constructor(@Inject(MAT_DIALOG_DATA) data: {id: string, timestamp: number | undefined}, private resource: ResourceService) {
    this.mediaResourceId = data.id
    this.timestamp = data.timestamp
    this.reload()
  }

  /**
   * Returns the URL pointing to the actual media resource.
   */
  get resourceUrl(): string {
    return `${this.resource.configuration.basePath}/api/resource/${this.mediaResourceId}`
  }

  /**
   * Reloads the currently active {@link MediaResource}.
   */
  public reload() {
    this.resource.getMediaResourceMetadata(this.mediaResourceId).subscribe(r => {
      this._mediaResource.next(r)
    })
  }
}