import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NotificationService } from '../services/notification.service';
import { ApiError } from '../models';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private notificationService: NotificationService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Došlo je do greške';

        if (error.error instanceof ErrorEvent) {
          // Client-side error
          errorMessage = `Greška: ${error.error.message}`;
        } else {
          // Server-side error
          const apiError = error.error as ApiError;

          switch (error.status) {
            case 400:
              if (apiError?.fieldErrors?.length > 0) {
                errorMessage = apiError.fieldErrors.map(fe => fe.message).join(', ');
              } else {
                errorMessage = apiError?.message || 'Neispravan zahtev';
              }
              break;
            case 404:
              errorMessage = apiError?.message || 'Resurs nije pronađen';
              break;
            case 409:
              errorMessage = apiError?.message || 'Konflikt - podaci već postoje';
              break;
            case 500:
              errorMessage = 'Greška na serveru. Pokušajte ponovo kasnije.';
              break;
            default:
              errorMessage = apiError?.message || `Greška: ${error.status}`;
          }
        }

        this.notificationService.error(errorMessage);
        return throwError(() => error);
      })
    );
  }
}
