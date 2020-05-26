import {Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Title} from '@angular/platform-browser';
import {Subscription} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TitleService {

  constructor(private translateService: TranslateService, private titleService: Title) {
  }

  setTitle(titleKeyToTranslate: string): void {
    const titleSubscription: Subscription = this.translateService.stream(titleKeyToTranslate).subscribe((title: string) => {
      if (title !== titleKeyToTranslate) {
        this.titleService.setTitle(title);
        if (titleSubscription) {
          titleSubscription.unsubscribe();
        }
      }
    });
  }
}
