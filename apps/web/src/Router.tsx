import {Route, Switch} from 'wouter';
import {routes} from '@/routes';
import {ContractPage, ContractsPage, HomePage, NotFoundPage, SourcesPage} from "@/pages";
import {AppLayout} from "@/layouts";

export const Router = () => {
    return (
        <AppLayout>
            <Switch>
                <Route path={routes.HOME} component={HomePage}/>
                <Route path={routes.CONTRACTS} component={ContractsPage}/>
                <Route path={routes.CONTRACT} component={ContractPage}/>
                <Route path={routes.SOURCES} component={SourcesPage}/>
                <Route component={NotFoundPage}/>
            </Switch>
        </AppLayout>
    );
};
