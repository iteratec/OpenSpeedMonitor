import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GraphiteIntegrationComponent } from './graphite-integration.component';
import {SharedMocksModule} from "../../../../../testing/shared-mocks.module";
import {ApplicationService} from "../../../../../services/application.service";
import {GrailsBridgeService} from "../../../../../services/grails-bridge.service";
import {GlobalOsmNamespace} from "../../../../../models/global-osm-namespace.model";
import {SharedModule} from "../../../../shared/shared.module";
import {NgxSmartModalService} from "ngx-smart-modal";
import {By} from "@angular/platform-browser";

describe('GraphiteIntegrationComponent', () => {
  let component: GraphiteIntegrationComponent;
  let fixture: ComponentFixture<GraphiteIntegrationComponent>;
  let ngxSmartModalService: NgxSmartModalService;
  let applicationService: ApplicationService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GraphiteIntegrationComponent ],
      imports: [
        SharedMocksModule,
        SharedModule
      ],
      providers: [
        ApplicationService,
        NgxSmartModalService,
        {provide: GrailsBridgeService, useClass: MockGrailsBridgeService}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    ngxSmartModalService = TestBed.get(NgxSmartModalService);
    applicationService = TestBed.get(ApplicationService);
    fixture = TestBed.createComponent(GraphiteIntegrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have an integration field', () => {
    const integrationField = fixture.debugElement.query(By.css(".integrations"));
    expect(integrationField.nativeElement).toBeTruthy();
  });

  it('should open and close modal correctly', () => {
    const integrationField = fixture.debugElement.query(By.css('.integrations li a'));
    integrationField.triggerEventHandler('click', null);

    fixture.detectChanges();

    let modalContent = fixture.nativeElement.querySelector('.nsm-content');
    expect(modalContent).toBeTruthy();
    expect(modalContent.innerText).toMatch("frontend.de.iteratec.osm.applicationDashboard.jobStatus.graphiteIntegration.modalTitle\n" +
      "frontend.de.iteratec.osm.applicationDashboard.jobStatus.graphiteIntegration.select\n" +
      "frontend.de.iteratec.osm.applicationDashboard.jobStatus.graphiteIntegration.select.noneAvailable\n" +
      "frontend.de.iteratec.osm.applicationDashboard.jobStatus.graphiteIntegration.footerTop\n" +
      "frontend.de.iteratec.osm.applicationDashboard.jobStatus.graphiteIntegration.footerBottom\n" +
      "frontend.default.button.savefrontend.default.button.cancel");
    let openedModals = ngxSmartModalService.getOpenedModals();
    expect(openedModals.length).toEqual(1);

    const cancelButton = fixture.debugElement.query(By.css('.buttons .btn-default'));
    spyOn(ngxSmartModalService, "close");
    cancelButton.triggerEventHandler('click', null);

    fixture.detectChanges();

    expect(ngxSmartModalService.close).toHaveBeenCalledWith('graphiteIntegrationModal');
  });

  it('should show available graphite servers', () => {
    applicationService.availableGraphiteServers$.next([
      {
        id: 1,
        address: "test.iteratec",
        port: 2003,
        protocol: "TCP",
        webAppAddress: "app.iteratec",
        prefix: "test"
      },
      {
        id: 2,
        address: "test2.iteratec",
        port: 2003,
        protocol: "TCP",
        webAppAddress: "app2.iteratec",
        prefix: "test2"
      }
    ]);
    const integrationField = fixture.debugElement.query(By.css('.integrations li a'));
    integrationField.triggerEventHandler('click', null);

    fixture.detectChanges();

    expect(component.availableGraphiteServers.length).toBe(2);
    const selectableServers = fixture.debugElement.queryAll(By.css('.server-overview .dropdown-item'));
    expect(selectableServers.length).toBe(2);
  });

  it('should show active graphite servers', () => {
    applicationService.jobHealthGraphiteServers$.next([
      {
        id: 1,
        address: "test.iteratec",
        port: 2003,
        protocol: "TCP",
        webAppAddress: "app.iteratec",
        prefix: "test"
      },
      {
        id: 2,
        address: "test2.iteratec",
        port: 2003,
        protocol: "TCP",
        webAppAddress: "app2.iteratec",
        prefix: "test2"
      }
    ]);
    const integrationField = fixture.debugElement.query(By.css('.integrations li a'));
    integrationField.triggerEventHandler('click', null);

    fixture.detectChanges();

    expect(component.jobHealthGraphiteServers.length).toBe(2);
    const activeServers = fixture.debugElement.queryAll(By.css('.server-overview .clickable-list li'));
    expect(activeServers.length).toBe(2);
  });

  it('should correctly add and remove graphite servers', () => {
    applicationService.availableGraphiteServers$.next([
      {
        id: 1,
        address: "test.iteratec",
        port: 2003,
        protocol: "TCP",
        webAppAddress: "app.iteratec",
        prefix: "test"
      },
      {
        id: 2,
        address: "test2.iteratec",
        port: 2003,
        protocol: "TCP",
        webAppAddress: "app2.iteratec",
        prefix: "test2"
      }
    ]);
    applicationService.jobHealthGraphiteServers$.next([]);
    const integrationField = fixture.debugElement.query(By.css('.integrations li a'));
    integrationField.triggerEventHandler('click', null);

    fixture.detectChanges();

    expect(component.availableGraphiteServers.length).toBe(2);
    expect(component.jobHealthGraphiteServers.length).toBe(0);

    const selectedServer = fixture.debugElement.query(By.css('.server-overview .dropdown-item'));
    selectedServer.triggerEventHandler('click', null);

    fixture.detectChanges();

    expect(component.availableGraphiteServers.length).toBe(1);
    expect(component.jobHealthGraphiteServers.length).toBe(1);
    expect(component.graphiteServersToAdd.length).toBe(1);

    const removeServerIcon = fixture.debugElement.query(By.css('.server-overview .clickable-list li i'));
    removeServerIcon.nativeElement.click();

    fixture.detectChanges();

    expect(component.availableGraphiteServers.length).toBe(2);
    expect(component.jobHealthGraphiteServers.length).toBe(0);
    expect(component.graphiteServersToAdd.length).toBe(0);
  });

  it('should correctly save changes', () => {
    applicationService.availableGraphiteServers$.next([
      {
        id: 1,
        address: "test.iteratec",
        port: 2003,
        protocol: "TCP",
        webAppAddress: "app.iteratec",
        prefix: "test"
      }
    ]);
    applicationService.jobHealthGraphiteServers$.next([
      {
      id: 2,
      address: "test2.iteratec",
      port: 2003,
      protocol: "TCP",
      webAppAddress: "app2.iteratec",
      prefix: "test2"
    }
    ]);
    const integrationField = fixture.debugElement.query(By.css('.integrations li a'));
    integrationField.triggerEventHandler('click', null);

    fixture.detectChanges();

    const selectedServer = fixture.debugElement.query(By.css('.server-overview .dropdown-item'));
    selectedServer.triggerEventHandler('click', null);
    const removeServerIcon = fixture.debugElement.queryAll(By.css('.server-overview .clickable-list li i'));

    removeServerIcon[0].nativeElement.click();

    fixture.detectChanges();

    expect(component.availableGraphiteServers.length).toBe(1);
    expect(component.jobHealthGraphiteServers.length).toBe(1);
    expect(component.graphiteServersToAdd.length).toBe(1);
    expect(component.graphiteServersToRemove.length).toBe(1);

    const saveButton = fixture.debugElement.query(By.css('.buttons .btn-primary'));
    spyOn(component, "save");
    saveButton.triggerEventHandler('click', null);

    fixture.detectChanges();

    expect(component.save).toHaveBeenCalled();
    expect(component.availableGraphiteServers.length).toBe(1);
    expect(component.availableGraphiteServers[0].id).toBe(2);
    expect(component.jobHealthGraphiteServers.length).toBe(1);
    expect(component.jobHealthGraphiteServers[0].id).toBe(1);
  });

});

class MockGrailsBridgeService extends GrailsBridgeService {
  globalOsmNamespace: GlobalOsmNamespace = {i18n: {lang: 'de'}, user: {loggedIn: true}};
}
